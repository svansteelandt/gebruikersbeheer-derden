package be.vdab.gebruikersbeheer.derden.web.internal.v1;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.exception.OrganizationNotFoundException;
import be.vdab.iam.oidc.WithJwtPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrganisatiesControllerGetAdminsForOrganizationIT extends BaseIT {

	@BeforeEach
	void beforeEach() {
		initIsimUser();
	}

	@Test
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void getAdminsForOrganization_userWithRoleBeheerdersDerden_isOk() throws Exception {
		given(adminDomainService.getGlobalIdsFromNonVirtualAdminsFor("123"))
				.willReturn(Collections.singletonList("456"));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/internal/gebruikersbeheer/v1/organisaties/123/admins")
						.accept(MediaType.ALL))
				.andExpect(status().isOk())
				.andExpect(content().string("[\"456\"]"));
	}

	@Test
	@WithJwtPrincipal(username = "JEFKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN_ZONDER_RRN)
	void getAdminsForOrganization_userWithRoleBeheerdersDerdenZonderRRN_isOk() throws Exception {
		given(adminDomainService.getGlobalIdsFromNonVirtualAdminsFor("123"))
				.willReturn(Collections.singletonList("456"));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/internal/gebruikersbeheer/v1/organisaties/123/admins")
						.accept(MediaType.ALL))
				.andExpect(status().isOk())
				.andExpect(content().string("[\"456\"]"));
	}

	@Test
	@WithJwtPrincipal(username = "CHAREL", roles = RoleNames.ROL_EXTRANET_EXPERT)
	void getAdminsForOrganization_userWithWrongRole_isForbidden() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/internal/gebruikersbeheer/v1/organisaties/123/admins")
						.accept(MediaType.ALL))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithJwtPrincipal(username = "JEFKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN_ZONDER_RRN)
	void getAdminsForOrganization_orgIdDoesNotExist_isNotFound() throws Exception {
		given(adminDomainService.getGlobalIdsFromNonVirtualAdminsFor("123"))
				.willThrow(OrganizationNotFoundException.class);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/internal/gebruikersbeheer/v1/organisaties/123/admins")
						.accept(MediaType.ALL))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithJwtPrincipal(username = "JEFKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN_ZONDER_RRN)
	void getAdminsForOrganization_emptyList_isOk() throws Exception {
		given(adminDomainService.getGlobalIdsFromNonVirtualAdminsFor("123"))
				.willReturn(Collections.emptyList());

		mockMvc.perform(MockMvcRequestBuilders.get("/api/internal/gebruikersbeheer/v1/organisaties/123/admins")
						.accept(MediaType.ALL))
				.andExpect(status().isOk())
				.andExpect(content().string("[]"));
	}


}