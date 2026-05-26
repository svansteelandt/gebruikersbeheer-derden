package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingSummaryDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingenZoekResultaatDto;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainSearch;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.domain.VestigingenZoekResultaat;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.iam.oidc.WithJwtPrincipal;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VestigingenRestControllerIT extends BaseIT {

	static final String IKP_HOOFDZETEL_NUMMER = "203";
	static final String VESTIGING_NAAM = "naam";

	static final String OE_NAAM = "OE 123";
	static final String IKP_VESTIGING_NUMMER = "000";
	static final String GEMEENTE = "g";
	static final String STRAAT = "str";
	static final String POSTCODE = "2900";
	static final String OE = "1";
	static final String KBO_NUMMER = "k";
	static final String GLOBAL_ID = "G1";
	static final String GLOBAL_ID2 = "G2";

	@Test
	@DisplayName(""" 
			   GIVEN all possible query params
			   WHEN /api/ui/vestigingen is called
			   THEN vestigingen are returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void erKanGezochtWordenMetGeldigeParametersEnTeveelResultaten() throws Exception {
		initIsimUser();

		var adminDomainObjecten = new ArrayList<AdminDomainObject>();
		var adminDomainObject1 = createAdminDomainObject(GLOBAL_ID);
		var adminDomainObject2 = createAdminDomainObject(GLOBAL_ID2);
		adminDomainObjecten.add(adminDomainObject1);
		adminDomainObjecten.add(adminDomainObject2);
		when(adminDomainService.findAdminDomainBySearchCriteria(any(AdminDomainSearch.class))).thenReturn(new VestigingenZoekResultaat(adminDomainObjecten, false));

		var response = mockMvc.perform(MockMvcRequestBuilders.get("/api/ui/vestigingen")
				.param("vestigingNaam", VESTIGING_NAAM)
				.param("straatNaam", STRAAT)
				.param("gemeente", GEMEENTE)
				.param("postcode", POSTCODE)
				.param("ikpHoofdzetelNummer", IKP_HOOFDZETEL_NUMMER)
				.param("ikpVestigingNummer", IKP_VESTIGING_NUMMER)
				.param("kboNummer", KBO_NUMMER)
				.param("oeNummer", OE));

		response.andExpect(MockMvcResultMatchers.status().isOk());

		verify(adminDomainService).findAdminDomainBySearchCriteria(any());

		var teVindenVestigingen = new ArrayList<VestigingSummaryDto>();
		var vestiging = new VestigingSummaryDto(GLOBAL_ID, VESTIGING_NAAM, GEMEENTE, POSTCODE, STRAAT, "203-000", KBO_NUMMER);
		teVindenVestigingen.add(vestiging);

		var zoekResultaat = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(), VestigingenZoekResultaatDto.class);
		assertThat(zoekResultaat.vestigingen()).usingRecursiveComparison().isEqualTo(teVindenVestigingen);
		assertThat(zoekResultaat.tooManyResults()).isTrue();
	}

	@Test
	@DisplayName(""" 
			   GIVEN all possible query params
			   WHEN /api/ui/vestigingen is called as application
			   THEN vestigingen are returned
			""")
	@WithJwtPrincipal(username = "derde-AP00123-app", isMachineToMachine = true)
	void erKanGezochtWordenAlsApplicationMetGeldigeParametersEnTeveelResultaten() throws Exception {
		var adminDomainObjecten = new ArrayList<AdminDomainObject>();
		var adminDomainObject1 = createAdminDomainObject(GLOBAL_ID);
		var adminDomainObject2 = createAdminDomainObject(GLOBAL_ID2);
		adminDomainObjecten.add(adminDomainObject1);
		adminDomainObjecten.add(adminDomainObject2);
		when(adminDomainService.findAdminDomainBySearchCriteria(any(AdminDomainSearch.class))).thenReturn(new VestigingenZoekResultaat(adminDomainObjecten, false));

		var response = mockMvc.perform(MockMvcRequestBuilders.get("/api/ui/vestigingen")
				.param("vestigingNaam", VESTIGING_NAAM)
				.param("straatNaam", STRAAT)
				.param("gemeente", GEMEENTE)
				.param("postcode", POSTCODE)
				.param("ikpHoofdzetelNummer", IKP_HOOFDZETEL_NUMMER)
				.param("ikpVestigingNummer", IKP_VESTIGING_NUMMER)
				.param("kboNummer", KBO_NUMMER)
				.param("oeNummer", OE));

		response.andExpect(MockMvcResultMatchers.status().isOk());

		verify(adminDomainService).findAdminDomainBySearchCriteria(any());

		var teVindenVestigingen = new ArrayList<VestigingSummaryDto>();
		var vestiging = new VestigingSummaryDto(GLOBAL_ID, VESTIGING_NAAM, GEMEENTE, POSTCODE, STRAAT, "203-000", KBO_NUMMER);
		teVindenVestigingen.add(vestiging);

		var zoekResultaat = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(), VestigingenZoekResultaatDto.class);
		assertThat(zoekResultaat.vestigingen()).usingRecursiveComparison().isEqualTo(teVindenVestigingen);
		assertThat(zoekResultaat.tooManyResults()).isTrue();
	}

	@Test
	@DisplayName(""" 
			   GIVEN all possible query params
			   WHEN /api/ui/vestigingen is called
			   THEN vestigingen are returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void erKanGezochtWordenMetGeldigeParameters() throws Exception {
		initIsimUser();

		var adminDomainObjecten = new ArrayList<AdminDomainObject>();
		var adminDomainObject1 = createAdminDomainObject(GLOBAL_ID);
		adminDomainObjecten.add(adminDomainObject1);
		when(adminDomainService.findAdminDomainBySearchCriteria(any(AdminDomainSearch.class))).thenReturn(new VestigingenZoekResultaat(adminDomainObjecten, false));

		var response = mockMvc.perform(MockMvcRequestBuilders.get("/api/ui/vestigingen")
				.param("vestigingNaam", VESTIGING_NAAM)
				.param("straatNaam", STRAAT)
				.param("gemeente", GEMEENTE)
				.param("postcode", POSTCODE)
				.param("ikpHoofdzetelNummer", IKP_HOOFDZETEL_NUMMER)
				.param("ikpVestigingNummer", IKP_VESTIGING_NUMMER)
				.param("kboNummer", KBO_NUMMER)
				.param("oeNummer", OE));

		response.andExpect(MockMvcResultMatchers.status().isOk());

		var adminDomainSearchArgumentCaptor = ArgumentCaptor.forClass(AdminDomainSearch.class);
		verify(adminDomainService).findAdminDomainBySearchCriteria(adminDomainSearchArgumentCaptor.capture());

		assertThat(adminDomainSearchArgumentCaptor.getValue().getName()).isEqualTo(VESTIGING_NAAM);
		assertThat(adminDomainSearchArgumentCaptor.getValue().getIkp()).isEqualTo(IKP_HOOFDZETEL_NUMMER);
		assertThat(adminDomainSearchArgumentCaptor.getValue().getIkpEnd()).isEqualTo(IKP_VESTIGING_NUMMER);
		assertThat(adminDomainSearchArgumentCaptor.getValue().getCity()).isEqualTo(GEMEENTE);
		assertThat(adminDomainSearchArgumentCaptor.getValue().getStreet()).isEqualTo(STRAAT);
		assertThat(adminDomainSearchArgumentCaptor.getValue().getPostalcode()).isEqualTo(POSTCODE);
		assertThat(adminDomainSearchArgumentCaptor.getValue().getOe()).isEqualTo(OE);
		assertThat(adminDomainSearchArgumentCaptor.getValue().getKboNummer()).isEqualTo(KBO_NUMMER);

		var teVindenVestigingen = new ArrayList<VestigingSummaryDto>();
		var vestiging = new VestigingSummaryDto(GLOBAL_ID, VESTIGING_NAAM, GEMEENTE, POSTCODE, STRAAT, "203-000", KBO_NUMMER);
		teVindenVestigingen.add(vestiging);

		var zoekResultaat = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(), VestigingenZoekResultaatDto.class);
		assertThat(zoekResultaat.vestigingen()).usingRecursiveComparison().isEqualTo(teVindenVestigingen);
		assertThat(zoekResultaat.tooManyResults()).isFalse();
	}

	@NotNull
	private static AdminDomainObject createAdminDomainObject(String globalId) {
		var adminDomainObject = new AdminDomainObject();
		adminDomainObject.setGlobalId(globalId);
		adminDomainObject.setDn(Dn.of("erglobalid=" + globalId));
		adminDomainObject.setName(VESTIGING_NAAM);
		adminDomainObject.setOeName(OE_NAAM);
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

	@Test
	@DisplayName(""" 
			   GIVEN existing vestiging id 1
			   WHEN /api/ui/vestigingen/1 is called
			   THEN vestiging is returned
			""")
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void eenVestigingKanOpgevraagdWorden() throws Exception {
		initIsimUser();

		var adminDomainObject = createAdminDomainObject(GLOBAL_ID);
		when(adminDomainService.findAdminDomainByDnWithRoles(any(Dn.class))).thenReturn(Optional.of(adminDomainObject));

		var response = mockMvc.perform(MockMvcRequestBuilders.get("/api/ui/vestigingen/1"));

		response.andExpect(MockMvcResultMatchers.status().isOk());

		var vestiging = new VestigingDto(adminDomainObject.getDn().getGlobalId(),
				adminDomainObject.getName(),
				adminDomainObject.getOeName(),
				adminDomainObject.getSamakks(),
				adminDomainObject.getRoles(),
				adminDomainObject.getAdministrators().stream().map(a -> a.getDn().getGlobalId()).toList());

		var resultaat = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(), VestigingDto.class);
		assertThat(resultaat).usingRecursiveComparison().isEqualTo(vestiging);
	}

	@Test
	@DisplayName(""" 
			   GIVEN user who doesnt have rights to assign a rol to multiple users
			   WHEN /api/ui/vestigingen/{globalIdOfOrganisation}/rollen/{globalIdOfRol}/gebruikers is called
			   THEN status 403 returned
			""")
	@WithJwtPrincipal()
	void gebruikerZonderRechtenOmRolToeTeKennen() throws Exception {
		initIsimUser();

		var response = mockMvc.perform(MockMvcRequestBuilders.put("/api/ui/vestigingen/1/rollen/2/gebruikers"));

		response.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	@Test
	@DisplayName(""" 
			   GIVEN user who doesnt have rights to clear the caches of a vestiging
			   WHEN /api/ui/vestigingen/{globalIdOfOrganisation}/cache is called
			   THEN status 403 returned
			""")
	void clearCacheVanVestigingZonderRechtenOmRolToeTeKennen() throws Exception {
		var response = mockMvc.perform(MockMvcRequestBuilders.delete("/api/ui/vestigingen/1/cache"));

		response.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	@Test
	@DisplayName(""" 
			   GIVEN existing vestiging id 1
			   WHEN /api/ui/vestigingen/1/additional-roles is called
			   THEN additional roles are returned
			""")
	@WithJwtPrincipal(username = "CHAREL", roles = RoleNames.ROL_EXTRANET_EXPERT)
	void additionalRoles() throws Exception {
		initIsimUser();

		var adminDomainObject = createAdminDomainObject(GLOBAL_ID);
		var role = new RoleObject();
		when(adminDomainService.findAdminDomainByDnWithRoles(any(Dn.class))).thenReturn(Optional.of(adminDomainObject));
		when(adminDomainService.getAdditionalRoles(any())).thenReturn(List.of(role));

		var response = mockMvc.perform(MockMvcRequestBuilders.get("/api/ui/vestigingen/1/additional-roles"));

		response.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$[0].globalId").value(role.getGlobalId()));
	}

	@Test
	@DisplayName(""" 
			   GIVEN existing vestiging id 1
			   WHEN /api/ui/vestigingen/1/additional-roles is called
			   THEN additional roles are added and removed
			""")
	@WithJwtPrincipal(username = "CHAREL", roles = RoleNames.ROL_EXTRANET_EXPERT)
	void saveAdditionalRoles() throws Exception {
		initIsimUser();

		var adminDomainObject = createAdminDomainObject(GLOBAL_ID);
		when(adminDomainService.findAdminDomainByDnWithRoles(any(Dn.class))).thenReturn(Optional.of(adminDomainObject));

		mockMvc.perform(post("/api/ui/vestigingen/1/additional-roles")
						.content("[]")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.with(csrf()))
				.andExpect(status().isOk());

		verify(adminDomainService).addAndRemoveRoles(adminDomainObject, List.of(), List.of());
	}
}
