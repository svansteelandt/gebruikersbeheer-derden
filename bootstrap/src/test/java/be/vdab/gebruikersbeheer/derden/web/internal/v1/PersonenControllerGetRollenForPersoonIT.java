package be.vdab.gebruikersbeheer.derden.web.internal.v1;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
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

class PersonenControllerGetRollenForPersoonIT extends BaseIT {

	private static final String URL = "/api/internal/gebruikersbeheer/v1/personen/{globalId}/rollen";

	private static final String GLOBAL_ID = "123456";
	private static final Dn PERSON_DN = Dn.of("erglobalid=" + GLOBAL_ID + ",ou=0,ou=people,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");
	private static final Dn PARENT_DN = Dn.of("parentDn");
	private static final String ROL_1 = "rol1";
	private static final String ROL_2 = "rol2";
	private static final String ROL_3 = "rol3";
	private static final String ROL_4 = "rol4";

	@BeforeEach
	void beforeEach() {
		initIsimUser();
	}

	@Test
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void getRollenForPersoon_globalIdDoesNotExist_returnsNotFound() throws Exception {
		when(personService.findPersonByDn(PERSON_DN, null)).thenReturn(Optional.empty());

		mockMvc.perform(get(URL, GLOBAL_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithJwtPrincipal(username = "CHAREL", roles = RoleNames.ROL_EXTRANET_EXPERT)
	void getRollenForPersoon_userWithWrongRole_returnsForbidden() throws Exception {
		mockMvc.perform(get(URL, GLOBAL_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithJwtPrincipal(username = "JEFKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN_ZONDER_RRN)
	void getRollenForPersoon_validGlobalIdAndOrganization_returnsRollenAndSamakks() throws Exception {
		PersonObject person = new PersonObject();
		person.setParentDn(PARENT_DN);
		RoleObject[] items = new RoleObject[]{createRoleObject(ROL_1, true), createRoleObject(ROL_2, true), createRoleObject(ROL_3, true), createRoleObject(ROL_4, false)};
		person.setRoles(List.of(items));
		when(personService.findPersonByDn(PERSON_DN, null)).thenReturn(Optional.of(person));

		MvcResult mvcResult = mockMvc.perform(get(URL, GLOBAL_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		Set<String> content = new ObjectMapper().readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {
		});
		assertThat(content).containsExactlyInAnyOrder(ROL_1, ROL_2, ROL_3);
	}

	private RoleObject createRoleObject(String roleName, boolean hasRole) {
		RoleObject roleObject = new RoleObject();
		roleObject.setRoleName(roleName);
		roleObject.setHasRole(hasRole);
		return roleObject;
	}
}
