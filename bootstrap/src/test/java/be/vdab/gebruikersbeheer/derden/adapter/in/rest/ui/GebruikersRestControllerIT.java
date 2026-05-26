package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerStatus;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerSummaryDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerZoekResultaatDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.ChangeLoginMethodCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.CreateGebruikerCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.EditGebruikerCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.WijsRollenToeAanGebruikerCommand;
import be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.model.Codes;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.LoginMethod;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonSearch;
import be.vdab.gebruikersbeheer.derden.domain.PersonStatus;
import be.vdab.gebruikersbeheer.derden.domain.PersonsZoekResultaat;
import be.vdab.gebruikersbeheer.derden.domain.RoleAssignmentResult;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.iam.oidc.WithJwtPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GebruikersRestControllerIT extends BaseIT {

	public static final String RRN = "78080216217";
	public static final String TELEFOON = "+3294568725";
	public static final String GSM = "+32471746885";
	public static final Dn PARENT_DN = Dn.of("erglobalid=1,");
	public static final Ikp IKP = Ikp.of(203000L);

	static final String VESTIGING_NAAM = "naam";
	static final String GEMEENTE = "g";
	static final String STRAAT = "str";
	static final String POSTCODE = "2900";
	static final String KBO_NUMMER = "k";
	static final String GLOBAL_ID = "G1";
	public static final String ROLE_ID = "1234";
	public static final String GEBRUIKER_NAAM = "KJESPERS";
	public static final String RIJKSREGISTER_NUMMER = RRN;
	public static final String VOORNAAM = "Jos";
	public static final String NAAM = "Vermeulen";
	public static final String VOLLEDIGE_NAAM = "Jos Vermeulen";
	public static final String EMAIL = "jos@vermeulen.be";
	public static final Dn PERSON_DN = new Dn("G1");
	public static final long OE_NUMMER = 1881;

	@BeforeEach
	void beforeEach() {
		initIsimUser();
	}

	@Test
	@DisplayName(""" 
			   GIVEN all possible query params
			   WHEN /api/ui/gebruikers is called
			   THEN gebruikers are returned
			   AND limitExceeded is set
			   AND the amount of results is limited
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void erKanGezochtWordenMetGeldigeParametersEnTeveelResultaten() throws Exception {
		var ikps = List.of(Ikp.of(203000L));
		var personObjects = new ArrayList<PersonObject>();
		var rol = createRole();
		var personObject1 = createPerson("email1@email.com", rol);
		var personObject2 = createPerson("email2@email.com", rol);
		personObjects.add(personObject1);
		personObjects.add(personObject2);
		when(personSearchService.zoek(any(PersonSearch.class))).thenReturn(new PersonsZoekResultaat(personObjects, ikps));

		var response = mockMvc.perform(get("/api/ui/gebruikers")
				.param("gebruikersNaam", GEBRUIKER_NAAM)
				.param("rijksregisterNummer", RIJKSREGISTER_NUMMER)
				.param("voornaam", VOORNAAM)
				.param("naam", NAAM)
				.param("volledigeNaam", VOLLEDIGE_NAAM)
				.param("email", EMAIL)
				.param("oeNummer", Long.toString(OE_NUMMER)));

		response.andExpect(status().isOk());

		var teVindenGebruikers = new ArrayList<GebruikerSummaryDto>();
		var gebruiker = getGebruiker(personObject1);
		teVindenGebruikers.add(gebruiker);

		var zoekResultaat = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(), GebruikerZoekResultaatDto.class);
		assertThat(zoekResultaat.gebruikers()).usingRecursiveComparison().isEqualTo(teVindenGebruikers);
		assertThat(zoekResultaat.tooManyResults()).isTrue();
	}

	@Test
	@DisplayName(""" 
			   GIVEN all possible query params
			   WHEN /api/ui/gebruikers is called as application
			   THEN gebruikers are returned
			   AND limitExceeded is set
			   AND the amount of results is limited
			""")
	@WithJwtPrincipal(username = "derde-AP00123-app", isMachineToMachine = true)
	void erKanGezochtWordenAlsApplicationMetGeldigeParametersEnTeveelResultaten() throws Exception {
		var ikps = List.of(Ikp.of(203000L));
		var personObjects = new ArrayList<PersonObject>();
		var rol = createRole();
		var personObject1 = createPerson("email1@email.com", rol);
		var personObject2 = createPerson("email2@email.com", rol);
		personObjects.add(personObject1);
		personObjects.add(personObject2);
		when(personSearchService.zoek(any(PersonSearch.class))).thenReturn(new PersonsZoekResultaat(personObjects, ikps));

		var response = mockMvc.perform(get("/api/ui/gebruikers")
				.param("gebruikersNaam", GEBRUIKER_NAAM)
				.param("rijksregisterNummer", RIJKSREGISTER_NUMMER)
				.param("voornaam", VOORNAAM)
				.param("naam", NAAM)
				.param("volledigeNaam", VOLLEDIGE_NAAM)
				.param("email", EMAIL)
				.param("oeNummer", Long.toString(OE_NUMMER)));

		response.andExpect(status().isOk());

		var teVindenGebruikers = new ArrayList<GebruikerSummaryDto>();
		var gebruiker = getGebruiker(personObject1);
		teVindenGebruikers.add(gebruiker);

		var zoekResultaat = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(), GebruikerZoekResultaatDto.class);
		assertThat(zoekResultaat.gebruikers()).usingRecursiveComparison().isEqualTo(teVindenGebruikers);
		assertThat(zoekResultaat.tooManyResults()).isTrue();
	}

	private static GebruikerSummaryDto getGebruiker(PersonObject personObject1) {
		return new GebruikerSummaryDto(personObject1.getDn().getGlobalId(),
				personObject1.getUserId(),
				personObject1.getVdabUid(),
				personObject1.getFullName(),
				personObject1.getProfileName(),
				personObject1.getDisplayRole(),
				personObject1.isSuspend(),
				LoginMethod.fromValue(personObject1.getLoginMethod()),
				personObject1.getRoles().stream().map(RoleObject::getGlobalId).toList(),
				personObject1.getBedrijfsnaam(),
				personObject1.getIkp().getDisplayValue(),
				GebruikerStatus.ACTIEF,
				personObject1.getDeleteDescription(),
				personObject1.getParentGlobalId(),
				personObject1.getEmailAddress());
	}

	@Test
	@DisplayName(""" 
			   GIVEN all possible query params
			   WHEN /api/ui/gebruikers is called
			   THEN gebruikers are returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void erKanGezochtWordenMetGeldigeParameters() throws Exception {
		var ikps = List.of(Ikp.of(203000L));
		var personObjects = new ArrayList<PersonObject>();
		var rol = createRole();
		var personObject = createPerson("email1@email.com", rol);
		personObjects.add(personObject);
		when(personSearchService.zoek(any(PersonSearch.class))).thenReturn(new PersonsZoekResultaat(personObjects, ikps));

		var response = mockMvc.perform(get("/api/ui/gebruikers")
				.param("gebruikersNaam", GEBRUIKER_NAAM)
				.param("rijksregisterNummer", RIJKSREGISTER_NUMMER)
				.param("voornaam", VOORNAAM)
				.param("naam", NAAM)
				.param("volledigeNaam", VOLLEDIGE_NAAM)
				.param("email", EMAIL)
				.param("oeNummer", Long.toString(OE_NUMMER)));

		response.andExpect(status().isOk());

		var teVindenGebruikers = new ArrayList<GebruikerSummaryDto>();
		var gebruiker = getGebruiker(personObject);
		teVindenGebruikers.add(gebruiker);

		var zoekResultaat = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(), GebruikerZoekResultaatDto.class);
		assertThat(zoekResultaat.gebruikers()).usingRecursiveComparison().isEqualTo(teVindenGebruikers);
		assertThat(zoekResultaat.tooManyResults()).isFalse();
	}

	@Test
	@DisplayName(""" 
			   GIVEN existing vestiging id
			   WHEN /api/ui/vestigingen/{vestigingId}/gebruikers is called
			   THEN gebruikers of vestiging are returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void gebruikersVanEenVestigingKunnenOpgevraagdWorden() throws Exception {
		var persons = new ArrayList<PersonObject>();
		var roleObject = createRole();
		roleObject.setHasRole(true);
		var personObject = createPerson(roleObject);
		persons.add(personObject);

		var adminDomainObject = createAdminDomainObject();
		when(adminDomainService.findAdminDomainByDnWithRoles(any(Dn.class))).thenReturn(Optional.of(adminDomainObject));
		when(personService.findNonMlpOpleidingPersonsFromOrganization(adminDomainObject)).thenReturn(persons);
		var response = mockMvc.perform(get("/api/ui/vestigingen/1/gebruikers"));

		response.andExpect(status().isOk());

		verify(adminDomainService).findAdminDomainByDnWithRoles(any(Dn.class));
		verify(personService).findNonMlpOpleidingPersonsFromOrganization(adminDomainObject);

		var gevondenGebruikers = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(), GebruikerSummaryDto[].class);
		assertThat(gevondenGebruikers).hasSize(1);
	}

	@Test
	@DisplayName(""" 
			   GIVEN existing vestiging id
			   WHEN /api/ui/vestigingen/{vestigingId}/gebruikers/verwijderd is called
			   THEN verwijderde gebruikers of vestiging are returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void verwijderdeGebruikersVanEenVestigingKunnenOpgevraagdWorden() throws Exception {
		var persons = new ArrayList<PersonObject>();
		var roleObject = createRole();
		roleObject.setHasRole(true);
		var personObject = createPerson(roleObject);
		persons.add(personObject);

		var adminDomainObject = createAdminDomainObject();
		when(adminDomainService.findAdminDomainByDnWithRoles(any(Dn.class))).thenReturn(Optional.of(adminDomainObject));
		when(personService.findPersonsInPrullenbakForIkpNummer(adminDomainObject.getIkp())).thenReturn(persons);
		var response = mockMvc.perform(get("/api/ui/vestigingen/1/gebruikers/verwijderd"));

		response.andExpect(status().isOk());

		verify(adminDomainService).findAdminDomainByDnWithRoles(any(Dn.class));
		verify(personService).findPersonsInPrullenbakForIkpNummer(adminDomainObject.getIkp());

		var gevondenGebruikers = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(), GebruikerSummaryDto[].class);
		assertThat(gevondenGebruikers).hasSize(1);
	}

	@Test
	@DisplayName(""" 
			   GIVEN existing vestiging id 1
			   WHEN /api/ui/vestigingen/1 is called
			   THEN vestiging is returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void eenVestigingKanOpgevraagdWorden() throws Exception {
		var adminDomainObject = createAdminDomainObject();
		when(adminDomainService.findAdminDomainByDnWithRoles(any(Dn.class))).thenReturn(Optional.of(adminDomainObject));

		var response = mockMvc.perform(get("/api/ui/vestigingen/1"));

		response.andExpect(status().isOk());

		var vestiging = new VestigingDto(adminDomainObject.getDn().getGlobalId(), adminDomainObject.getName(), adminDomainObject.getOeName(), adminDomainObject.getSamakks(), adminDomainObject.getRoles(), adminDomainObject.getAdministrators().stream().map(a -> a.getDn().getGlobalId()).toList());

		var resultaat = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(), VestigingDto.class);
		assertThat(resultaat).usingRecursiveComparison().isEqualTo(vestiging);
	}

	@Test
	@DisplayName("""
			GIVEN existing person and user has no roles to delete a user
			WHEN vestigingen/{vestigingGlobalId}/gebruikers/{gebruikerGlobalId} is called
			THEN 403 status is returned
			""")
	void verwijderenVanGebruikerIndienGebruikerDaarvoorGeenRolHeeft() throws Exception {
		var response = mockMvc.perform(delete("/api/ui/vestigingen/1/gebruikers/1"));
		response.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("""
			GIVEN user who doesnt have role ROL_CVS_RFI
			WHEN gebruikers/{globalId}/login-methode is called
			THEN 403 status is returned
			""")
	void changeLoginMethodReturns403IfNotAllowed() throws Exception {
		var response = mockMvc.perform(delete("/api/ui/gebruikers/1/login-methode"));
		response.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("""
			GIVEN user who doesnt have roles to restore a gebruiker
			WHEN PUT /gebruikers/{globalId}/status is called
			THEN 403 status is returned
			""")
	void restoreGebruikerMethodReturns403IfNotAllowed() throws Exception {
		var response = mockMvc.perform(put("/api/ui/gebruikers/1/status"));
		response.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("""
			WHEN POST /vestigingen/{globalId}/gebruikers is called
			THEN 200 status is returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void createGebruiker() throws Exception {
		RoleObject role = createRole();
		setupCreateUserMocks(role);

		mockMvc.perform(post("/api/ui/vestigingen/{id}/gebruikers", GLOBAL_ID)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.accept(MediaType.APPLICATION_JSON_VALUE)
						.content(objectMapper.writeValueAsString(createGebruikerCommand())))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(gebruikerDto(role))));
	}

	private void setupCreateUserMocks(RoleObject role) throws Exception {
		Codes codes = new Codes();

		AdminDomainObject adminDomainObject = createAdminDomainObject();
		when(roleService.findRoleByGlobalId(ROLE_ID)).thenReturn(Optional.of(role));
		when(adminDomainService.findAdminDomainByDnWithRoles(any(Dn.class))).thenReturn(Optional.of(adminDomainObject));
		when(personCreateService.create(eq(GLOBAL_ID), any(), any())).thenReturn(createPerson(role));
		when(genericRestServiceApi.geefCodes43(anyString(), isNull(), isNull(), isNull(), isNull())).thenReturn(codes);
	}

	@Test
	@DisplayName("""
					WHEN a user is created with an unknown property
					THEN this property is ignored
					BECAUSE we want to be able to remove properties and stay backwards compatible
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void whenCreatingGebruikerWithUnknownProperty_thenUnknownPropertyIsIgnored() throws Exception {
		RoleObject role = createRole();
		setupCreateUserMocks(role);

		Map<String, Object> createGebruikerCommandWithUknownProperty = objectMapper.convertValue(createGebruikerCommand(), new TypeReference<>() {
		});
		createGebruikerCommandWithUknownProperty.put("foo", "bar");

		mockMvc.perform(post("/api/ui/vestigingen/{id}/gebruikers", GLOBAL_ID)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.accept(MediaType.APPLICATION_JSON_VALUE)
						.content(objectMapper.writeValueAsString(createGebruikerCommandWithUknownProperty)))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(gebruikerDto(role))));
	}


	@Test
	@DisplayName("""
			WHEN PUT /vestigingen/{globalId}/gebruikers is called
			THEN 200 status is returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void editGebruiker() throws Exception {
		RoleObject role = createRole();
		PersonObject person = createPerson(role, "suspendOmschrijving");
		when(personService.findPersonWithRolesByGlobalId(GLOBAL_ID)).thenReturn(Optional.of(person));

		var response = mockMvc.perform(put("/api/ui/gebruikers/{globalId}", GLOBAL_ID)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.accept(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(editGebruikerCommand())));

		response.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(editedGebruikerDto(role))));
	}

	@Test
	@DisplayName("""
			WHEN PUT /gebruikers/{globalId}/login-methode is called
			THEN 200 status is returned
			""")
	@WithJwtPrincipal(username = "JANTJE", roles = RoleNames.ROL_CVS_RFI)
	void changeLoginMethod() throws Exception {
		when(personService.changeLoginMethod(GLOBAL_ID, LoginMethod.ACM.getValue())).thenReturn(true);

		var command = new ChangeLoginMethodCommand(LoginMethod.ACM);

		var response = mockMvc.perform(put("/api/ui/gebruikers/{globalid}/login-methode", GLOBAL_ID)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(command)));

		response.andExpect(status().isOk());
	}

	@Test
	@DisplayName("""
			WHEN PUT /gebruikers/{globalId}/login-methode is called
			THEN 200 status is returned
			""")
	@WithJwtPrincipal(username = "JANTJE", roles = RoleNames.ROL_CVS_RFI)
	void changeLoginMethod_personServiceFails() throws Exception {
		when(personService.changeLoginMethod(GLOBAL_ID, LoginMethod.ACM.getValue())).thenReturn(false);

		var command = new ChangeLoginMethodCommand(LoginMethod.ACM);

		var response = mockMvc.perform(put("/api/ui/gebruikers/{globalid}/login-methode", GLOBAL_ID)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(command)));

		response.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("""
			WHEN PUT /gebruikers/{globalId}/rollen is called
			THEN 200 status is returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void changeUserRoles() throws Exception {
		var result = new RoleAssignmentResult(true, false, false);
		when(personChangeRolesService.changeRoles(GLOBAL_ID, List.of("een rol"), "cvs rol")).thenReturn(result);

		var command = new WijsRollenToeAanGebruikerCommand(List.of("een rol"), "cvs rol");

		var response = mockMvc.perform(put("/api/ui/gebruikers/{globalid}/rollen", GLOBAL_ID)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.accept(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(command)));

		response.andExpect(status().isOk())
				.andExpect(jsonPath("$.someRolesAreChanged").value(true))
				.andExpect(jsonPath("$.minAdminsReached").value(false))
				.andExpect(jsonPath("$.maxAdminsReached").value(false));
	}

	@Test
	@DisplayName("""
			WHEN DELETE /gebruikers/{globalId}/wachtwoord is called
			THEN 200 status is returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void deletePassword() throws Exception {
		when(personService.findPersonByDn(any(), any())).thenReturn(Optional.of(createPerson(createRole())));

		var response = mockMvc.perform(delete("/api/ui/gebruikers/{globalid}/wachtwoord", GLOBAL_ID)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.accept(MediaType.APPLICATION_JSON_VALUE));

		response.andExpect(status().isOk());
		verify(accountService).resetPassword(any(), any());
	}

	@Test
	@DisplayName("""
			WHEN DELETE /gebruikers/{globalId}/wachtwoord is called
			THEN 200 status is returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void deletePassword_emailIsBlank() throws Exception {
		when(personService.findPersonByDn(any(), any())).thenReturn(Optional.of(createPerson("", createRole())));

		var response = mockMvc.perform(delete("/api/ui/gebruikers/{globalid}/wachtwoord", GLOBAL_ID)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.accept(MediaType.APPLICATION_JSON_VALUE));

		response.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("""
			WHEN PUT /gebruikers/{globalId}/status is called
			THEN 200 status is returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void restoreUser() throws Exception {
		var response = mockMvc.perform(put("/api/ui/gebruikers/{globalid}/status", GLOBAL_ID)
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.accept(MediaType.APPLICATION_JSON_VALUE));

		response.andExpect(status().isOk());
		verify(personRestoreService).restore(GLOBAL_ID);
	}

	@Test
	@DisplayName("""
			WHEN DELETE /vestigingen/{vestigingGlobalId}/gebruikers/{gebruikerGlobalId} is called
			THEN 200 status is returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void deleteUser() throws Exception {
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(createAdminDomainObject()));
		when(personService.findPersonByDn(any(), any())).thenReturn(Optional.of(createPerson(createRole())));

		var response = mockMvc.perform(delete("/api/ui/vestigingen/{vestigingGlobalId}/gebruikers/{gebruikerGlobalId}", GLOBAL_ID, GLOBAL_ID)
				.with(csrf()));

		response.andExpect(status().isOk());
		verify(personService).deletePerson(any(), any());
	}

	private static PersonObject createPerson(RoleObject roleObject) {
		return createPerson(EMAIL, roleObject);
	}

	private static PersonObject createPerson(RoleObject roleObject, String suspendOmschrijving) {
		return createPerson(EMAIL, roleObject, suspendOmschrijving);
	}

	private static PersonObject createPerson(String email, RoleObject roleObject) {
		return createPerson(email, roleObject, null);
	}

	private static PersonObject createPerson(String email, RoleObject roleObject, String suspendReason) {
		var personObject = new PersonObject();
		personObject.setFirstName(VOORNAAM);
		personObject.setLastName(NAAM);
		personObject.setDn(PERSON_DN);
		personObject.setParentDn(PARENT_DN);
		personObject.setUserId(GEBRUIKER_NAAM);
		personObject.setVdabUid(GEBRUIKER_NAAM);
		personObject.setIkp(IKP);
		personObject.setProfileName(GEBRUIKER_NAAM);
		personObject.setRoles(List.of(roleObject));
		personObject.setSuspend(true);
		personObject.setSuspendOmschrijving(suspendReason);
		personObject.setLoginMethod(LoginMethod.ACM.getValue());
		personObject.setNationalNumber(RRN);
		personObject.setEmailAddress(email);
		personObject.setPhone(TELEFOON);
		personObject.setMobile(GSM);
		personObject.setStatus(PersonStatus.ACTIVE);
		return personObject;
	}


	private static CreateGebruikerCommand createGebruikerCommand() {
		return new CreateGebruikerCommand(
				VOORNAAM,
				NAAM,
				RRN,
				EMAIL,
				TELEFOON,
				GSM,
				List.of(ROLE_ID),
				false);
	}

	private static EditGebruikerCommand editGebruikerCommand() {
		return new EditGebruikerCommand(
				"Josette",
				"Vermalen",
				"josette@vermalen.be",
				"+3293453638",
				"+32475463112",
				false,
				"Test reden");
	}

	private static GebruikerDto.Builder gebruikerDtoBuilder(RoleObject role) {
		return GebruikerDto.builder()
				.globalId(null)
				.vestigingId("1")
				.userId(GEBRUIKER_NAAM)
				.vdabUid(GEBRUIKER_NAAM)
				.fullName(String.join(" ", VOORNAAM, NAAM))
				.firstName(VOORNAAM)
				.lastName(NAAM)
				.profileName(GEBRUIKER_NAAM)
				.displayRole("vdab rol naam")
				.suspend(true)
				.loginMethod(LoginMethod.ACM)
				.rollen(List.of(role))
				.rijkregisterNummerIsToegankelijk(true)
				.rijkregisterNummer(RRN)
				.email(EMAIL)
				.telefoon(TELEFOON)
				.gsm(GSM)
				.ikp("203-000")
				.bedrijfsNaam(null)
				.suspendOmschrijving(null)
				.cvsRol(null);
	}

	private static GebruikerDto gebruikerDto(RoleObject role) {
		return gebruikerDtoBuilder(role).build();
	}

	@NotNull
	private static GebruikerDto editedGebruikerDto(RoleObject roleObject) {
		return gebruikerDtoBuilder(roleObject)
				.fullName("Josette Vermalen")
				.firstName("Josette")
				.lastName("Vermalen")
				.suspend(false)
				.suspendOmschrijving(null)
				.email("josette@vermalen.be")
				.telefoon("+3293453638")
				.gsm("+32475463112")
				.build();
	}

	@NotNull
	private static RoleObject createRole() {
		var roleObject = new RoleObject();
		roleObject.setDn(Dn.of("erglobalid=1,"));
		roleObject.setVdabRoleName("vdab rol naam");
		roleObject.setVdabRoleDescription("vdab rol omschrijving");
		roleObject.setHasRole(true);
		return roleObject;
	}

	@NotNull
	private static AdminDomainObject createAdminDomainObject() {
		var adminDomainObject = new AdminDomainObject();
		adminDomainObject.setGlobalId(GLOBAL_ID);
		adminDomainObject.setDn(Dn.of("erglobalid=" + GLOBAL_ID));
		adminDomainObject.setName(VESTIGING_NAAM);
		adminDomainObject.setPostalcode(POSTCODE);
		adminDomainObject.setCity(GEMEENTE);
		adminDomainObject.setStreet(STRAAT);
		adminDomainObject.setIkp(Ikp.of(203000L));
		adminDomainObject.setKboNummer(KBO_NUMMER);
		adminDomainObject.setSamakks(Set.of("AKKOORD_1"));
		var roleObject = new RoleObject();
		adminDomainObject.setRoles(List.of(roleObject));
		return adminDomainObject;
	}
}
