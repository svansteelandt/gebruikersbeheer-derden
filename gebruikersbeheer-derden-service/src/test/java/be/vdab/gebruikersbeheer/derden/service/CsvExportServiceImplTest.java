package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvExportServiceImplTest {

	private final static Dn ROLE_DN_1 = Dn.of("erGlobalId=123");
	private final static Dn ROLE_DN_2 = Dn.of("erGlobalId=456");
	private final static Dn ROLE_DN_3 = Dn.of("erGlobalId=789");
	private final static Ikp DUMMY_IKP = Ikp.of("123456");

	@InjectMocks
	private CsvExportServiceImpl csvExportService;

	@Mock
	private HttpServletResponse response;
	@Mock
	private IsimLdapManager isimLdapManager;
	@Mock
	private RoleService roleService;

	private StringWriter writer;

	@BeforeEach
	void setUp() throws IOException {
		writer = new StringWriter();
		when(response.getWriter()).thenReturn(new PrintWriter(writer));
		csvExportService = new CsvExportServiceImpl(isimLdapManager, roleService);

		RoleObject roleObject1 = mock(RoleObject.class);
		lenient().when(roleObject1.getVdabRoleName()).thenReturn("Rol 1");
		RoleObject roleObject2 = mock(RoleObject.class);
		lenient().when(roleObject2.getVdabRoleName()).thenReturn("Rol-2");
		RoleObject roleObject3 = mock(RoleObject.class);
		lenient().when(roleObject3.getVdabRoleName()).thenReturn("Rol_3");
		lenient().when(roleService.findRoleByDN(ROLE_DN_1)).thenReturn(Optional.of(roleObject1));
		lenient().when(roleService.findRoleByDN(ROLE_DN_2)).thenReturn(Optional.of(roleObject2));
		lenient().when(roleService.findRoleByDN(ROLE_DN_3)).thenReturn(Optional.of(roleObject3));
	}

	@Test
	@DisplayName("GIVEN an organisation with 2 derde users and 1 auditor user " +
			"WHEN a CSV export is requested for that organisation " +
			"THEN the export is created with only the derde users")
	void test() throws Exception {
		IsimLdapPerson person1 = createPerson("derde", "Meys", "Jeroen", "jmeys", "12602", "Casa Maison", List.of(ROLE_DN_1), "0478731201", "jmeys@vdab.be");
		IsimLdapPerson person2 = createPerson("derde", "Van Hoye", "Anton", "avhoye", "12601", "Bxl 3", List.of(ROLE_DN_1, ROLE_DN_2), "0678121321", "avhoye@vdab.be");
		IsimLdapPerson person3 = createPerson("auditor", "Geudens", "Filip", "fgeudens", "12601", "Auditors", List.of(ROLE_DN_2), "0487654321", "fgeudens@vdab.be");
		when(isimLdapManager.getPersonsInOrganizationAndSuborganisations(DUMMY_IKP, DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(List.of(person1, person2, person3));

		csvExportService.sendCsv(response, DUMMY_IKP, "export.csv");

		assertThat(writer.getBuffer()).hasToString(csvContentsOf("src/test/resources/csv/happyPath2users.csv"));
	}

	@Test
	@DisplayName("GIVEN an organisation with no users " +
			"WHEN a CSV export is requested for that organisation " +
			"THEN the export only contains the headers")
	void test2() throws Exception {
		when(isimLdapManager.getPersonsInOrganizationAndSuborganisations(DUMMY_IKP, DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Collections.emptyList());

		csvExportService.sendCsv(response, DUMMY_IKP, "export.csv");

		assertThat(writer.getBuffer()).hasToString(csvContentsOf("src/test/resources/csv/nousers.csv"));
	}

	@Test
	@DisplayName("GIVEN an organisation with a user containing a \" character " +
			"WHEN a CSV export is requested for that organisation " +
			"THEN the quote is properly escaped")
	void test3() throws Exception {
		IsimLdapPerson person = createPerson("derde", "\"Meys", "Jeroen", "jmeys", "12602", "Casa\" Ma\"\"son", List.of(ROLE_DN_1), "0478731201", "jmeys@vdab.be");
		when(isimLdapManager.getPersonsInOrganizationAndSuborganisations(DUMMY_IKP, DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(List.of(person));

		csvExportService.sendCsv(response, DUMMY_IKP, "export.csv");

		assertThat(writer.getBuffer()).hasToString(csvContentsOf("src/test/resources/csv/doublequote.csv"));
	}

	private IsimLdapPerson createPerson(String employeeType, String ln, String fn, String profile, String ikp, String org, List<Dn> roles, String phone, String mail) {
		IsimLdapPerson person = mock(IsimLdapPerson.class);
		lenient().when(person.getEmployeeType()).thenReturn(employeeType);
		lenient().when(person.getNaam()).thenReturn(ln);
		lenient().when(person.getVoornaam()).thenReturn(fn);
		lenient().when(person.getVdabUid()).thenReturn(profile);
		lenient().when(person.getIkp()).thenReturn(Ikp.of(ikp));
		lenient().when(person.getBedrijfsnaam()).thenReturn(org);
		lenient().when(person.getRollen()).thenReturn(new LinkedHashSet<>(roles));
		lenient().when(person.getTelefoon()).thenReturn(phone);
		lenient().when(person.getEmail()).thenReturn(mail);
		return person;
	}

	private String csvContentsOf(String fileName) throws IOException {
		Path path = Path.of(fileName);
		return String.join("\n", Files.readAllLines(path)) + "\n";
	}
}