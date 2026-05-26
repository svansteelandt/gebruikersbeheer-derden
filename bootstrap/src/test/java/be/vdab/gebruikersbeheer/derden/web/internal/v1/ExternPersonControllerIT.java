package be.vdab.gebruikersbeheer.derden.web.internal.v1;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.iam.oidc.WithJwtPrincipal;
import be.vdab.iam.oidc.authentication.SecurityDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ExternPersonControllerIT extends BaseIT {

	private static final String USERID = "AVHOYE14";

	private AdminDomainObject adminDomainObject = getAdminObject();

	@BeforeEach
	void setUp() {
		PersonObject ingelogdePerssoon = new PersonObject();
		ingelogdePerssoon.setUserId(USERID);
		ingelogdePerssoon.setDn(new Dn("erglobalid=8382071954155097461,ou=0,ou=people,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be"));

		when(personService.findPersonByGebruikersnaam(anyString(), anyList())).thenReturn(ingelogdePerssoon);
		when(adminDomainService.findAdminDomainsForAdministrator(any(Dn.class), anyList())).thenReturn(List.of(adminDomainObject));
		when(adminDomainService.findAdminDomainByDnWithRoles(adminDomainObject.getDn())).thenReturn(Optional.of(adminDomainObject));
	}

	@Test
	@WithJwtPrincipal(username = USERID, domain = SecurityDomain.DERDE)
	@DisplayName("""
			WHEN Creating derde user as external admin WITH Belgian phone number without countrycode
			THEN user is created AND user is redirected to overview organization page
			""")
	void createDerdeWithBelgianPhoneNumber() throws Exception {
		Dn newPersonDn = new Dn("erglobalid=123,ou=0,ou=people");
		PersonObject newPersonObject = new PersonObject();
		newPersonObject.setDn(newPersonDn);

		when(personService.insertPerson(any(AdminDomainObject.class), any(PersonObject.class), anyBoolean())).thenReturn(newPersonDn);
		when(personService.findPersonByDn(newPersonDn, adminDomainObject)).thenReturn(Optional.of(newPersonObject));

		mockMvc.perform(post("/extern/organization/{id}/person/create", adminDomainObject.getDn().getGlobalId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("person.nationalNumber", "78021020580")
						.param("person.firstName", "First")
						.param("person.lastName", "Last")
						.param("person.emailAddress", "first.last@local.vdab.be")
						.param("person.mobile", "+32486112233")
						.param("person.phone", "022503611"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/extern/organization/1400427702632520801/person/overzicht"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "930133778"})
	@WithJwtPrincipal(username = USERID, domain = SecurityDomain.DERDE)
	@DisplayName("""
			WHEN Creating derde user as external admin WITH incorrect or missing rrnr
			THEN user is not created AND model contains message THAT rrnr is not correct/missing
			""")
	void createGebruikerWithMissingOrIncorrectRijksregisternummer(String rrnr) throws Exception {
		AdminDomainObject adminDomainObject = getAdminObject();

		mockMvc.perform(post("/extern/organization/{id}/person/create", adminDomainObject.getDn().getGlobalId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("person.nationalNumber", rrnr)
						.param("person.firstName", "First")
						.param("person.lastName", "Last")
						.param("person.emailAddress", "first.last@local.vdab.be")
						.param("person.mobile", "+32486112233")
						.param("person.phone", "022503611"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(view().name("/extern/person/nieuwe_gebruiker"))
				.andExpect(model().attributeHasErrors("personCommand"))
				.andExpect(model().attributeHasFieldErrors("personCommand", "person.nationalNumber"));
	}

	@Test
	@WithJwtPrincipal(username = USERID, domain = SecurityDomain.DERDE)
	@DisplayName("""
			WHEN Creating derde user as external admin AND organization contains already user for rrnr
			THEN user is not created AND model contains message THAT rrnr already exists
			""")
	void createGebruikerWithRijksregisternummerThatAlreadyExists() throws Exception {
		AdminDomainObject adminDomainObject = getAdminObject();

		String rrnr = "78021020976";
		when(personService.rrnExists(rrnr, adminDomainObject.getDn())).thenReturn(true);

		mockMvc.perform(post("/extern/organization/{id}/person/create", adminDomainObject.getDn().getGlobalId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("person.nationalNumber", "78021020976")
						.param("person.firstName", "First")
						.param("person.lastName", "Last")
						.param("person.emailAddress", "first.last@local.vdab.be")
						.param("person.mobile", "+32486112233")
						.param("person.phone", "022503611"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(view().name("/extern/person/nieuwe_gebruiker"))
				.andExpect(model().attributeHasErrors("personCommand"))
				.andExpect(model().attributeHasFieldErrors("personCommand", "person.nationalNumber"));
	}

	@Test
	@WithJwtPrincipal(username = USERID, domain = SecurityDomain.DERDE)
	@DisplayName("""
			WHEN Creating derde user as external admin AND prullenbak contains already user for rrnr for ikp
			THEN user is not created AND user stays on the creating page
			""")
	void createGebruikerWithRijksregisternummerThatAlreadyExistsInPrullenbak() throws Exception {
		AdminDomainObject adminDomainObject = getAdminObject();

		String rrnr = "78021020976";
		when(personService.getPersonDnInPrullenbakForRrnAndIkpNummer(rrnr, adminDomainObject.getIkp())).thenReturn(new Dn("erglobalid=123,ou=0,ou=people"));

		mockMvc.perform(post("/extern/organization/{id}/person/create", adminDomainObject.getDn().getGlobalId())
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("person.nationalNumber", "78021020976")
						.param("person.firstName", "First")
						.param("person.lastName", "Last")
						.param("person.emailAddress", "first.last@local.vdab.be")
						.param("person.mobile", "+32486112233")
						.param("person.phone", "022503611"))
				.andExpect(status().is2xxSuccessful())
				.andExpect(view().name("/extern/person/nieuwe_gebruiker"));
	}

	private AdminDomainObject getAdminObject() {
		AdminDomainObject adminDomainObject = new AdminDomainObject();
		adminDomainObject.setDn(new Dn("erglobalid=1400427702632520801,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be"));
		adminDomainObject.setGlobalId(adminDomainObject.getDn().getGlobalId());
		adminDomainObject.setIkp(Ikp.of("10006667003"));

		return adminDomainObject;
	}
}
