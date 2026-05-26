package be.vdab.gebruikersbeheer.derden.extern.controller.v1;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.IsimUserData;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.extension.FlashMap;
import be.vdab.gebruikersbeheer.derden.extern.view.PersonCommand;
import be.vdab.gebruikersbeheer.derden.monitoring.MonitoringService;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.service.AccountService;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.derden.test.TestUtil;
import be.vdab.gebruikersbeheer.derden.util.isim.support.IsimSessionService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternPersonControllerTest {

	private static final String PERSON_UID = "uid";
	private static final String PERSON_GLOBAL_ID = "789012";
	private static final Dn PERSON_DN = TestUtil.isimDn(PERSON_GLOBAL_ID);
	private static final String ORGANIZATION_GLOBAL_ID = "123456";
	private static final String ORGANIZATION_REDIRECT = "redirect:/extern/organization/" + ORGANIZATION_GLOBAL_ID + "/person/overzicht";
	private static final String GET_RIGHTS_REDIRECT = "/extern/person/gebruiker_wijzig_rechten";

	private static final Dn ORGANIZATION_DN = TestUtil.isimDn(ORGANIZATION_GLOBAL_ID);
	private static final String TAG_DERDE_EXTERN = "DerdeExtern";
	private static final Ikp IKP = Ikp.of(123465000L);

	@InjectMocks
	private ExternPersonController externPersonController;

	@Mock
	private AdminDomainService adminDomainService;

	@Mock
	private ApplicationProperties applicationProperties;

	@Mock
	private PersonService personService;

	@Mock
	private RoleService roleService;

	@Mock
	private AccountService accountService;

	@Mock
	IsimSessionService isimSessionService;

	@Mock
	MonitoringService monitoringService;

	@Mock
	private RoleObject role1, role2;

	@Mock
	private Model model;

	private HttpServletRequest request;

	private IsimUserContextManager isimUserContextManager;

	@BeforeEach
	void setUp() {
		isimUserContextManager = new IsimUserContextManager(isimSessionService, monitoringService);

		lenient().when(applicationProperties.createAdminDomainDn(ORGANIZATION_GLOBAL_ID)).thenReturn(ORGANIZATION_DN);
		lenient().when(applicationProperties.createPersonDn(PERSON_GLOBAL_ID)).thenReturn(PERSON_DN);

		request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletWebRequest(request));
	}

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void findRoles() {
		List<RoleObject> roles = List.of(role1, role2);
		when(roleService.findRoles()).thenReturn(roles);

		assertThat(externPersonController.findRoles()).isEqualTo(roles);
	}

	@Test
	void overview() {
		AdminDomainObject adminDomain = createAdminDomain();
		when(adminDomainService.findAdminDomainByDnWithRoles(ORGANIZATION_DN)).thenReturn(Optional.of(adminDomain));
		when(adminDomainService.findAdminDomainsForAdministrator(any())).thenReturn(List.of(adminDomain, new AdminDomainObject()));

		List<PersonObject> persons = createPersons();
		when(personService.findPersonsFromOrganization(adminDomain)).thenReturn(persons);

		List<PersonObject> personsInPrullenbak = createPersonsInPrullenbak();
		when(personService.findPersonsInPrullenbakForIkpNummer(IKP)).thenReturn(personsInPrullenbak);

		IsimUserData userData = mock(IsimUserData.class);
		when(userData.getHoofdGebruikersnaam()).thenReturn(PERSON_UID);
		when(userData.getPersonDn()).thenReturn(PERSON_DN);
		isimUserContextManager.setContext(userData);

		String result = externPersonController.overview(ORGANIZATION_GLOBAL_ID, model);
		assertThat(result).isEqualTo("/extern/person/overzicht");

		verify(model).addAttribute("admindomain", adminDomain);
		verify(model).addAttribute("admindomainsize", 2);
		verify(model).addAttribute("ingelogde", PERSON_UID);
		verify(model).addAttribute("persons", persons);
		verify(model).addAttribute("personsinprullenbak", personsInPrullenbak);
		verifyNoMoreInteractions(model);

		assertThat(adminDomain.getRoles()).allMatch(role -> role.getTags().contains(TAG_DERDE_EXTERN));
		assertThat(personsInPrullenbak).extracting(PersonObject::getDeleteDescription).containsExactly(
				"Verwijderd door 'Fons De Spons' (Administrator)",
				"Verwijderd door VDAB",
				"IKP-toegangen afgesloten");
	}

	@Test
	void overview_adminDomainNull() {
		when(adminDomainService.findAdminDomainByDnWithRoles(ORGANIZATION_DN)).thenReturn(Optional.empty());

		String result = externPersonController.overview(ORGANIZATION_GLOBAL_ID, null);
		assertThat(result).isEqualTo("redirect:/extern/organization/");

		verifyNoInteractions(model);
	}

	@Test
	@DisplayName("""
			GIVEN vestigingId
			AND admindomain has no admins
			THEN RETURN roles WITH selected ADMIN role
			""")
	void getDataPersonCreationIfNoOtherAdmins() {
		AdminDomainObject adminDomain = createAdminDomain();

		getDataPersonCreation(adminDomain);

		RoleObject adminRoleObject = adminDomain.getPossibleRolesForPerson().stream()
				.filter(RoleObject::isAdminRole)
				.findFirst()
				.orElseThrow(() -> new AssertionError("No admin role found"));

		assertThat(adminRoleObject.getHasRole()).isTrue();
	}

	@Test
	@DisplayName("""
			GIVEN vestigingId
			AND admindomain has admins
			THEN RETURN roles WITH ADMIN role not selected
			""")
	void getDataPersonCreationIfOtherAdminsExists() {
		AdminDomainObject adminDomain = createAdminDomain();
		PersonObject administrator = getDataPersonCreation("Admin", adminDomain.getIkp(), null);

		adminDomain.getAdministrators().add(administrator);

		getDataPersonCreation(adminDomain);

		RoleObject adminRoleObject = adminDomain.getPossibleRolesForPerson().stream()
				.filter(RoleObject::isAdminRole)
				.findFirst()
				.orElseThrow(() -> new AssertionError("No admin role found"));

		assertThat(adminRoleObject.getHasRole()).isFalse();
	}

	private void getDataPersonCreation(AdminDomainObject adminDomain) {
		RoleObject adminRole = new RoleObject();
		adminRole.setRoleName("DOMAIN ADMINS");
		adminRole.setAdminRole(true);

		adminDomain.getRoles().add(adminRole);
		when(this.adminDomainService.findAdminDomainByDnWithRoles(ORGANIZATION_DN)).thenReturn(Optional.of(adminDomain));

		String result = externPersonController.create(ORGANIZATION_GLOBAL_ID, model);
		assertThat(result).isEqualTo("/extern/person/nieuwe_gebruiker");

		verify(model).addAttribute("admindomain", adminDomain);
		verify(model).addAttribute(eq("personCommand"), any(PersonCommand.class));
		verifyNoMoreInteractions(model);
	}

	@Test
	@DisplayName("""
			GIVEN vestigingId
			AND admindomain does not exists
			THEN RETURN to organization paga
			""")
	void createPersonAdminDomainNotFound() {
		String result = externPersonController.create(ORGANIZATION_GLOBAL_ID, model);
		assertThat(result).isEqualTo("redirect:/extern/organization/");
	}

	@Test
	void restorePerson() {
		PersonObject person = createPersonInPrullenbak();
		AdminDomainObject adminDomain = createAdminDomain();
		when(personService.findPersonByDn(PERSON_DN, null)).thenReturn(Optional.of(person));
		when(adminDomainService.findAdminDomainByDn(ORGANIZATION_DN)).thenReturn(Optional.of(adminDomain));

		String result = externPersonController.restorePerson(ORGANIZATION_GLOBAL_ID, PERSON_GLOBAL_ID, model);
		assertThat(result).isEqualTo("/extern/person/restore");

		verify(model).addAttribute("person", person);
		verify(model).addAttribute("admindomain", adminDomain);
		verifyNoMoreInteractions(model);
	}

	@Test
	void restorePerson_personIsNull() {
		when(personService.findPersonByDn(PERSON_DN, null)).thenReturn(Optional.empty());
		when(adminDomainService.findAdminDomainByDn(ORGANIZATION_DN)).thenReturn(Optional.of(createAdminDomain()));

		String result = externPersonController.restorePerson(ORGANIZATION_GLOBAL_ID, PERSON_GLOBAL_ID, model);
		assertThat(result).isEqualTo(ORGANIZATION_REDIRECT);

		verifyNoInteractions(model);
	}

	@Test
	void restorePerson_adminDomainIsNull() {
		when(personService.findPersonByDn(PERSON_DN, null)).thenReturn(Optional.of(createPersonInPrullenbak()));
		when(adminDomainService.findAdminDomainByDn(ORGANIZATION_DN)).thenReturn(Optional.empty());

		String result = externPersonController.restorePerson(ORGANIZATION_GLOBAL_ID, PERSON_GLOBAL_ID, model);
		assertThat(result).isEqualTo(ORGANIZATION_REDIRECT);

		verifyNoInteractions(model);
	}

	@Test
	void restorePerson_personHasDifferentIkp() {
		when(personService.findPersonByDn(PERSON_DN, null)).thenReturn(Optional.of(getDataPersonCreation("jefke", Ikp.of(1000L), null)));
		when(adminDomainService.findAdminDomainByDn(ORGANIZATION_DN)).thenReturn(Optional.of(createAdminDomain()));

		String result = externPersonController.restorePerson(ORGANIZATION_GLOBAL_ID, PERSON_GLOBAL_ID, model);
		assertThat(result).isEqualTo(ORGANIZATION_REDIRECT);

		verifyNoInteractions(model);
	}

	@Test
	void restorePersonAction() {
		PersonObject person = createPersonInPrullenbak();
		when(personService.findPersonByDn(PERSON_DN, null)).thenReturn(Optional.of(person));
		when(adminDomainService.findAdminDomainByDn(ORGANIZATION_DN)).thenReturn(Optional.of(createAdminDomain()));

		when(personService.restorePerson(person)).thenReturn(true);

		String result = externPersonController.restorePersonAction(ORGANIZATION_GLOBAL_ID, PERSON_GLOBAL_ID);
		assertThat(result).isEqualTo(ORGANIZATION_REDIRECT);

		verify(personService).restorePerson(person);
		verify(accountService).resetPassword(person.getUserId(), person.getEmailAddress());

		Map<String, FlashMap.Message> current = FlashMap.getCurrent(request);
		assertThat(current.values()).extracting(FlashMap.Message::getKey, FlashMap.Message::getType, FlashMap.Message::getText).contains(
				tuple("passwordReset", FlashMap.MessageType.notificationInfo, person.getFullName() + " zal een e-mail ontvangen met de instructies om een wachtwoord te kiezen."),
				tuple("restorePerson", FlashMap.MessageType.notificationInfo, "marcel MARCEL")
		);
	}

	@Test
	void restorePersonAction_geenEmail() {
		PersonObject person = createPersonInPrullenbak();
		person.setEmailAddress(null);
		when(personService.findPersonByDn(PERSON_DN, null)).thenReturn(Optional.of(person));
		when(adminDomainService.findAdminDomainByDn(ORGANIZATION_DN)).thenReturn(Optional.of(createAdminDomain()));

		when(personService.restorePerson(person)).thenReturn(true);

		String result = externPersonController.restorePersonAction(ORGANIZATION_GLOBAL_ID, PERSON_GLOBAL_ID);
		assertThat(result).isEqualTo(ORGANIZATION_REDIRECT);

		verify(personService).restorePerson(person);
		verify(accountService, never()).resetPassword(person.getUserId(), person.getEmailAddress());

		Map<String, FlashMap.Message> current = FlashMap.getCurrent(request);
		assertThat(current.values()).extracting(FlashMap.Message::getKey, FlashMap.Message::getType, FlashMap.Message::getText).contains(
				tuple("passwordReset", FlashMap.MessageType.notificationErrorWarning, "Er kan geen nieuw wachtwoord voor '" + person.getFullName() + "' aangevraagd worden aangezien er geen e-mailadres gekend is."),
				tuple("restorePerson", FlashMap.MessageType.notificationInfo, "marcel MARCEL")
		);
	}

	@Test
	void restorePersonAction_personIsNull() {
		when(personService.findPersonByDn(PERSON_DN, null)).thenReturn(Optional.empty());
		when(adminDomainService.findAdminDomainByDn(ORGANIZATION_DN)).thenReturn(Optional.of(createAdminDomain()));

		String result = externPersonController.restorePersonAction(ORGANIZATION_GLOBAL_ID, PERSON_GLOBAL_ID);
		assertThat(result).isEqualTo(ORGANIZATION_REDIRECT);

		verifyNoInteractions(model);
	}

	@Test
	void restorePersonAction_adminDomainIsNull() {
		when(personService.findPersonByDn(PERSON_DN, null)).thenReturn(Optional.of(createPersonInPrullenbak()));
		when(adminDomainService.findAdminDomainByDn(ORGANIZATION_DN)).thenReturn(Optional.empty());

		String result = externPersonController.restorePersonAction(ORGANIZATION_GLOBAL_ID, PERSON_GLOBAL_ID);
		assertThat(result).isEqualTo(ORGANIZATION_REDIRECT);

		verifyNoInteractions(model);
	}

	@Test
	void restorePersonAction_personHasDifferentIkp() {
		when(personService.findPersonByDn(PERSON_DN, null)).thenReturn(Optional.of(getDataPersonCreation("jefke", Ikp.of(1000L), null)));
		when(adminDomainService.findAdminDomainByDn(ORGANIZATION_DN)).thenReturn(Optional.of(createAdminDomain()));

		String result = externPersonController.restorePersonAction(ORGANIZATION_GLOBAL_ID, PERSON_GLOBAL_ID);
		assertThat(result).isEqualTo(ORGANIZATION_REDIRECT);

		verifyNoInteractions(model);
	}

	@Test
	@DisplayName("""
			GIVEN organizationId and personId
			WHEN calling rights
			THEN new page is 'gebruiker_wijzig_rechten' with ingelogde user, 
			personCommand with person with personId and admindomain with adminDomain with organizationId on the model 
			""")
	void getRights() {
		IsimUserData userData = IsimUserData.builder().hoofdGebruikersnaam("HOOFDGEBRUIKER").build();
		AdminDomainObject adminDomain = createAdminDomain();
		PersonObject person = getDataPersonCreation("joske", IKP, null);

		isimUserContextManager.setContext(userData);
		when(adminDomainService.findAdminDomainByDnWithRoles(ORGANIZATION_DN)).thenReturn(Optional.of(adminDomain));
		when(personService.findPersonByDn(PERSON_DN, adminDomain)).thenReturn(Optional.of(person));
		String result = externPersonController.rights(ORGANIZATION_GLOBAL_ID, PERSON_GLOBAL_ID, model);

		assertThat(result).isEqualTo(GET_RIGHTS_REDIRECT);

		verify(model).addAttribute("ingelogde", userData.getHoofdGebruikersnaam());
		verify(model).addAttribute(eq("personCommand"), any(PersonCommand.class));
		verify(model).addAttribute("admindomain", adminDomain);
	}

	private AdminDomainObject createAdminDomain() {
		AdminDomainObject adminDomain = new AdminDomainObject(ORGANIZATION_DN);
		adminDomain.setRoles(rolesWithDifferentTags());
		adminDomain.setIkp(IKP);
		return adminDomain;
	}

	private List<RoleObject> rolesWithDifferentTags() {
		return List.of(roleWithTag(TAG_DERDE_EXTERN), roleWithTag(TAG_DERDE_EXTERN), roleWithTag("DerdeIntern"), new RoleObject());
	}

	private static RoleObject roleWithTag(String tag) {
		RoleObject role = new RoleObject();
		role.addTag(tag);
		return role;
	}

	private static List<PersonObject> createPersonsInPrullenbak() {
		return List.of(
				createPersonInPrullenbak(),
				getDataPersonCreation("jozef", IKP, "Verwijderd door 'Fons De Spons' (VDAB)"),
				getDataPersonCreation("marie", IKP, "IKP-toegangen afgesloten")
		);
	}

	@NotNull
	private static PersonObject createPersonInPrullenbak() {
		return getDataPersonCreation("marcel", IKP, "Verwijderd door 'Fons De Spons' (Administrator)");
	}

	private static List<PersonObject> createPersons() {
		return List.of(
				getDataPersonCreation("joske", IKP, null),
				getDataPersonCreation("jefke", IKP, null)
		);
	}

	private static PersonObject getDataPersonCreation(String naam, Ikp ikp, String deleteDescription) {
		PersonObject personObject = new PersonObject(TestUtil.uniqueIsimDn(naam));
		personObject.setUserId(naam);
		personObject.setIkp(ikp);
		personObject.setDeleteDescription(deleteDescription);
		personObject.setEmailAddress(naam + "@yahoo.com");
		personObject.setFirstName(naam);
		personObject.setLastName(naam.toUpperCase());
		return personObject;
	}
}