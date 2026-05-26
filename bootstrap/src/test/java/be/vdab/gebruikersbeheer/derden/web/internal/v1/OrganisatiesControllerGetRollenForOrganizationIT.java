package be.vdab.gebruikersbeheer.derden.web.internal.v1;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.iam.oidc.WithJwtPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrganisatiesControllerGetRollenForOrganizationIT extends BaseIT {

	private static final String URL = "/api/internal/gebruikersbeheer/v1/organisaties/{orgId}/rollen";

	private static final String ORG_ID = "123456";
	private static final String ROL_1 = "rol1";
	private static final String ROL_2 = "rol2";
	private static final List<RoleObject> ADMIN_DOMAIN_ROLES;

	static {
		RoleObject[] items = new RoleObject[]{createRoleObject(ROL_1), createRoleObject(ROL_2), createRoleObject(RoleNames.ROL_DOMAIN_ADMINS)};
		ADMIN_DOMAIN_ROLES = List.of(items);
	}

	@BeforeEach
	void beforeEach() {
		initIsimUser();
	}

	@Test
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void getRollenForOrganization_orgIdDoesNotExist_returnsNotFound() throws Exception {
		when(adminDomainService.findAdminDomainByOrgIdWithRoles(ORG_ID)).thenReturn(Optional.empty());

		mockMvc.perform(get(URL, ORG_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithJwtPrincipal(username = "CHAREL", roles = RoleNames.ROL_EXTRANET_EXPERT)
	void getRollenForOrganization_userWithWrongRole_returnsForbidden() throws Exception {
		mockMvc.perform(get(URL, ORG_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithJwtPrincipal(username = "JEFKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN_ZONDER_RRN)
	void getRollenForOrganization_validOrg_returnsRollenAndSamakks() throws Exception {
		AdminDomainObject adminDomain = new AdminDomainObject();
		adminDomain.setRoles(ADMIN_DOMAIN_ROLES);
		when(adminDomainService.findAdminDomainByOrgIdWithRoles(ORG_ID)).thenReturn(Optional.of(adminDomain));

		MvcResult mvcResult = mockMvc.perform(get(URL, ORG_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		Set<String> content = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
		});
		assertThat(content).containsExactlyInAnyOrder(ROL_1, ROL_2);
	}

	private static RoleObject createRoleObject(String roleName) {
		RoleObject roleObject = new RoleObject();
		roleObject.setRoleName(roleName);
		return roleObject;
	}
}
