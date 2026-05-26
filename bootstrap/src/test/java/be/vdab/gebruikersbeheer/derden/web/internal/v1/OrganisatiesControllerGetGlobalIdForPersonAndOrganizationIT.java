package be.vdab.gebruikersbeheer.derden.web.internal.v1;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.exception.OrganizationNotFoundException;
import be.vdab.iam.oidc.WithJwtPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrganisatiesControllerGetGlobalIdForPersonAndOrganizationIT extends BaseIT {

	@BeforeEach
	void beforeEach() {
		initIsimUser();
	}

	@Test
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void getGlobalIdForPerson_orgIdDoesNotExist_returnsNotFound() throws Exception {
		given(personService.getGlobalIdFromPersonBy("999999999", new Insz("85081236549")))
				.willThrow(OrganizationNotFoundException.class);

		mockMvc.perform(post("/api/internal/gebruikersbeheer/v1/organisaties/999999999/personen/globalId")
						.content("{\"inszNummer\": \"85081236549\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void getGlobalIdForPerson_inszDoesNotExist_returnsNotFound() throws Exception {
		given(personService.getGlobalIdFromPersonBy("203", new Insz("85081236549")))
				.willThrow(PersonNotFoundException.class);

		mockMvc.perform(post("/api/internal/gebruikersbeheer/v1/organisaties/203/personen/globalId")
						.content("{\"inszNummer\": \"85081236549\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void getGlobalIdForPerson_inszIsInvalid_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/internal/gebruikersbeheer/v1/organisaties/203/personen/globalId")
						.content("{\"inszNummer\": \"inszNummer\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Invalid INSZ number"));
	}

	@Test
	@WithJwtPrincipal(username = "CHAREL", roles = RoleNames.ROL_EXTRANET_EXPERT)
	void getGlobalIdForPerson_userWithWrongRole_returnsForbidden() throws Exception {
		mockMvc.perform(post("/api/internal/gebruikersbeheer/v1/organisaties/203/personen/globalId")
						.content("{\"inszNummer\": \"85081236549\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void getGlobalIdForPerson_validOrgIdAndInsz_returnsGlobalId() throws Exception {
		given(personService.getGlobalIdFromPersonBy("25441", new Insz("85081236549")))
				.willReturn("5867098487073444991");

		mockMvc.perform(post("/api/internal/gebruikersbeheer/v1/organisaties/25441/personen/globalId")
						.content("{\"inszNummer\": \"85081236549\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string("5867098487073444991"));
	}
}
