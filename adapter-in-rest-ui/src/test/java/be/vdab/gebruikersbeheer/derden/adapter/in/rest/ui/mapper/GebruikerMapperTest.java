package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.CvsCodeDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerStatus;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerSummaryDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.CreateGebruikerCommand;
import be.vdab.gebruikersbeheer.derden.core.CodesPort;
import be.vdab.gebruikersbeheer.derden.core.domain.Code;
import be.vdab.gebruikersbeheer.derden.domain.LoginMethod;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonStatus;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GebruikerMapperTest {

	@Mock
	CodesPort codesPort;

	@InjectMocks
	GebruikerMapper gebruikerMapper;

	@Test
	@DisplayName("""
			GIVEN valid list of PersonObject
			WHEN map is called
			THEN valid list of GebruikerDto is returned
			""")
	void mapAdminDomainObjects() {
		var roleObject = createRole();
		roleObject.setHasRole(true);
		var personObject = createPerson(roleObject, false, false, PersonStatus.ACTIVE);

		var gebruiker = new GebruikerSummaryDto(
				personObject.getDn().getGlobalId(),
				personObject.getUserId(),
				personObject.getVdabUid(),
				"jos vermeulen",
				personObject.getProfileName(),
				personObject.getDisplayRole(),
				personObject.isSuspend(),
				LoginMethod.fromValue(personObject.getLoginMethod()),
				personObject.getRoles().stream().map(RoleObject::getGlobalId).toList(),
				personObject.getBedrijfsnaam(),
				personObject.getIkp().getDisplayValue(),
				GebruikerStatus.ACTIEF,
				personObject.getDeleteDescription(),
				personObject.getParentGlobalId(),
				personObject.getEmailAddress()
		);
		assertThat(gebruikerMapper.map(List.of(personObject))).usingRecursiveComparison().isEqualTo(List.of(gebruiker));
	}

	@Test
	@DisplayName("""
			GIVEN valid CreateGebruikerCommand
			WHEN mapToPerson is called
			THEN valid person is returned
			""")
	void mapToPerson() {
		var command = new CreateGebruikerCommand("Jos", "vermeulen", "78080216217", "test@vdab.be", "03657777", "0486337477", List.of(), false);
		var expectedPerson = new PersonObject();
		expectedPerson.setFirstName(command.firstName());
		expectedPerson.setLastName(command.lastName());
		expectedPerson.setNationalNumber(command.rrnr());
		expectedPerson.setEmailAddress(command.email());
		expectedPerson.setPhone(command.phone());
		expectedPerson.setMobile(command.mobile());
		expectedPerson.setNoRrn(command.hasNoRrnr());
		var person = gebruikerMapper.mapToPerson(command);
		assertThat(person).usingRecursiveComparison().isEqualTo(expectedPerson);
	}

	@Test
	@DisplayName("""
			GIVEN valid CreateGebruikerCommand for person without rrnr
			WHEN mapToPerson is called
			THEN valid person is returned
			""")
	void mapToPersonWithoutRrnr() {
		var command = new CreateGebruikerCommand("Jos", "vermeulen", null, "test@vdab.be", "03657777", "0486337477", List.of(), true);
		var expectedPerson = new PersonObject();
		expectedPerson.setFirstName(command.firstName());
		expectedPerson.setLastName(command.lastName());
		expectedPerson.setNationalNumber("");
		expectedPerson.setEmailAddress(command.email());
		expectedPerson.setPhone(command.phone());
		expectedPerson.setMobile(command.mobile());
		expectedPerson.setNoRrn(command.hasNoRrnr());
		var person = gebruikerMapper.mapToPerson(command);
		assertThat(person).usingRecursiveComparison().isEqualTo(expectedPerson);
	}

	@Test
	@DisplayName("""
			GIVEN valid person
			WHEN toGebruikerSummary is called
			THEN valid GebruikerSummary is returned
			""")
	void toGebruikerSummary() {
		var roleObject = createRole();
		roleObject.setHasRole(true);

		var person = createPerson(roleObject, false, false, PersonStatus.INACTIVE);

		var expectedGebruikerSummary = new GebruikerSummaryDto(person.getDn().getGlobalId(),
				person.getUserId(),
				person.getVdabUid(),
				person.getFullName(),
				person.getProfileName(),
				person.getDisplayRole(),
				person.isSuspend(),
				LoginMethod.fromValue(person.getLoginMethod()),
				person.getRoles().stream().map(RoleObject::getGlobalId).toList(),
				person.getBedrijfsnaam(),
				person.getIkp().getDisplayValue(),
				GebruikerStatus.PASSIEF,
				person.getDeleteDescription(),
				person.getParentGlobalId(),
				person.getEmailAddress()
		);

		var gebruikerSummary = gebruikerMapper.toGebruikerSummary(person);
		assertThat(gebruikerSummary).usingRecursiveComparison().isEqualTo(expectedGebruikerSummary);
	}

	private static RoleObject createRole() {
		var roleObject = new RoleObject();
		roleObject.setDn(Dn.of("erglobalid=1,"));
		roleObject.setVdabRoleName("vdab rol naam");
		roleObject.setVdabRoleDescription("vdab rol omschrijving");
		return roleObject;
	}

	@Test
	@DisplayName("""
			GIVEN valid person of which the rrnr may be available
			WHEN toGebruiker is called
			THEN valid Gebruiker is returned
			""")
	void toGebruikerMetRrnr() {
		var roleObject = createRole();
		roleObject.setHasRole(true);

		var person = createPerson(roleObject, true, false, PersonStatus.SUSPENDED);

		var expectedGebruiker = new GebruikerDto(
				person.getDn().getGlobalId(),
				person.getParentGlobalId(),
				person.getUserId(),
				person.getVdabUid(),
				person.getFullName(),
				person.getFirstName(),
				person.getLastName(),
				person.getProfileName(),
				person.getDisplayRole(),
				person.isSuspend(),
				LoginMethod.fromValue(person.getLoginMethod()),
				person.getRoles(),
				person.isRrnAccessible(),
				person.getNationalNumber(),
				person.getEmailAddress(),
				person.getPhone(),
				person.getMobile(),
				person.getIkp() != null ? person.getIkp().getDisplayValue() : null,
				person.getBedrijfsnaam(),
				person.getSuspendOmschrijving(),
				null
		);

		var gebruiker = gebruikerMapper.toGebruiker(person);
		assertThat(gebruiker).usingRecursiveComparison().isEqualTo(expectedGebruiker);
	}

	@Test
	@DisplayName("""
			GIVEN valid person of which the rrnr may NOT be available
			WHEN toGebruiker is called
			THEN valid Gebruiker is returned
			""")
	void toGebruikerZonderRrnr() {
		var roleObject = createRole();
		roleObject.setHasRole(true);

		var person = createPerson(roleObject, false, true, PersonStatus.ACTIVE);

		var expectedGebruiker = new GebruikerDto(
				person.getDn().getGlobalId(),
				person.getParentGlobalId(),
				person.getUserId(),
				person.getVdabUid(),
				person.getFullName(),
				person.getFirstName(),
				person.getLastName(),
				person.getProfileName(),
				person.getDisplayRole(),
				person.isSuspend(),
				LoginMethod.fromValue(person.getLoginMethod()),
				person.getRoles(),
				person.isRrnAccessible(),
				null,
				person.getEmailAddress(),
				person.getPhone(),
				person.getMobile(),
				person.getIkp() != null ? person.getIkp().getDisplayValue() : null,
				person.getBedrijfsnaam(),
				person.getSuspendOmschrijving(),
				new CvsCodeDto(person.getVdabCvsRol(), "lang")
		);

		var cvsRol = new Code();
		cvsRol.setWaarde(person.getVdabCvsRol());
		cvsRol.setLangLabel(expectedGebruiker.cvsRol().omschrijving());

		var cvsRollen = List.of(cvsRol);
		when(codesPort.getCVSRollen()).thenReturn(cvsRollen);
		var gebruiker = gebruikerMapper.toGebruiker(person);
		assertThat(gebruiker).usingRecursiveComparison().isEqualTo(expectedGebruiker);
	}

	private static PersonObject createPerson(RoleObject roleObject, boolean isRrnAccessible, boolean metCvsRol, PersonStatus status) {
		var personObject = mock(PersonObject.class);
		lenient().when(personObject.getDn()).thenReturn(Dn.of("G1"));
		lenient().when(personObject.getParentGlobalId()).thenReturn("1");
		lenient().when(personObject.getUserId()).thenReturn("userId1");
		lenient().when(personObject.getVdabUid()).thenReturn("user1");
		lenient().when(personObject.getFirstName()).thenReturn("jos");
		lenient().when(personObject.getLastName()).thenReturn("vermeulen");
		lenient().when(personObject.getFullName()).thenReturn("jos vermeulen");
		lenient().when(personObject.isRrnAccessible()).thenReturn(isRrnAccessible);
		lenient().when(personObject.getNationalNumber()).thenReturn("78080216217");
		lenient().when(personObject.getIkp()).thenReturn(Ikp.of(203000L));
		lenient().when(personObject.getProfileName()).thenReturn("p1");
		lenient().when(personObject.getRoles()).thenReturn(List.of(roleObject));
		lenient().when(personObject.isSuspend()).thenReturn(true);
		lenient().when(personObject.getLoginMethod()).thenReturn(LoginMethod.ACM.getValue());
		lenient().when(personObject.getBedrijfsnaam()).thenReturn("Colruyt");
		lenient().when(personObject.getStatus()).thenReturn(status);
		lenient().when(personObject.getEmailAddress()).thenReturn("k@k.com");
		lenient().when(personObject.getMobile()).thenReturn("0486");
		lenient().when(personObject.getPhone()).thenReturn("03658");
		lenient().when(personObject.getSuspendOmschrijving()).thenReturn("blabla");
		lenient().when(personObject.getDeleteDescription()).thenReturn("tekstje");
		lenient().when(personObject.getVdabCvsRol()).thenReturn(metCvsRol ? "mlp" : null);
		return personObject;
	}
}
