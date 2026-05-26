package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper.VestigingMapper;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingSummaryDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingenZoekResultaatDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.WijsGebruikersToeAanRolCommand;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainSearch;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.domain.VestigingenZoekResultaat;
import be.vdab.gebruikersbeheer.derden.exception.SearchCriteriaVerplichtException;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.AssignUsersToRolService;
import be.vdab.gebruikersbeheer.derden.service.cache.PersonCacheService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VestigingenRestControllerTest {
	static final String IKP_HOOFDZETEL_NUMMER = "203";
	static final String VESTIGING_NAAM = "naam";
	static final String IKP_VESTIGING_NUMMER = "000";
	static final String GEMEENTE = "g";
	static final String STRAAT = "str";
	static final String POSTCODE = "2900";
	static final Long OE = 1L;
	static final String KBO_NUMMER = "k";
	static final String VESTIGING_ID = "1";
	static final String ROL_ID = "2";
	static final String GEBRUIKER_ID = "3";
	static final int LIMIT = 10;

	@Mock
	AdminDomainService adminDomainService;
	@Mock
	VestigingDto vestigingDto;
	@Mock
	VestigingMapper vestigingMapper;
	@Mock
	PersonCacheService cacheService;
	@Mock
	ApplicationProperties applicationProperties;
	@Mock
	AssignUsersToRolService assignUsersToRolService;
	@InjectMocks
	VestigingenRestController vestigingenRestController;

	@Test
	@DisplayName("""
			GIVEN searchcriteria
			WHEN zoek is called
			THEN vestiging summary list is returned
			""")
	void zoek() throws Exception {
		when(applicationProperties.getUiSearchLimit()).thenReturn(LIMIT);
		var adminDomains = List.of(new AdminDomainObject());
		var vestigingSummaries = new ArrayList<VestigingSummaryDto>();
		when(adminDomainService.findAdminDomainBySearchCriteria(any(AdminDomainSearch.class))).thenReturn(new VestigingenZoekResultaat(adminDomains, true));
		when(vestigingMapper.map(anyList())).thenReturn(vestigingSummaries);
		var teVindenZoekResultaat = new VestigingenZoekResultaatDto(vestigingSummaries, false);
		assertThat(vestigingenRestController.zoek(VESTIGING_NAAM, STRAAT, GEMEENTE, POSTCODE, IKP_HOOFDZETEL_NUMMER,
				IKP_VESTIGING_NUMMER, KBO_NUMMER, OE)).isEqualTo(teVindenZoekResultaat);
	}

	@Test
	@DisplayName("""
			GIVEN no searchcriteria
			WHEN zoek is called
			THEN SearchCriteriaVerplichtException is thrown
			""")
	void zoekZonderCriteria() {
		assertThatThrownBy(() -> vestigingenRestController.zoek(null, null, null, null, null,
				null, null, null)).isInstanceOf(SearchCriteriaVerplichtException.class);
	}

	@Test
	@DisplayName("""
			GIVEN globalId of vestiging
			WHEN getVestiging is called
			THEN vestiging is returned
			""")
	void getVestiging() {
		var adminDomain = new AdminDomainObject();
		var dn = Dn.of("1");
		when(applicationProperties.createAdminDomainDn("1")).thenReturn(dn);
		when(adminDomainService.findAdminDomainByDnWithRoles(any(Dn.class))).thenReturn(Optional.of(adminDomain));
		when(vestigingMapper.map(adminDomain)).thenReturn(vestigingDto);
		assertThat(vestigingenRestController.getVestiging("1")).isEqualTo(vestigingDto);
	}

	@Test
	@DisplayName("""
			GIVEN globalId of vestiging, globalId of role and valid command
			WHEN wijzigAanRolToegewezenGebruikers is called
			THEN vestiging is returned
			""")
	void wijzigAanRolToegewezenGebruikers() throws Exception {
		var command = new WijsGebruikersToeAanRolCommand(List.of(GEBRUIKER_ID));
		vestigingenRestController.wijzigAanRolToegewezenGebruikers(VESTIGING_ID, ROL_ID, command);
		verify(assignUsersToRolService).assign(VESTIGING_ID, ROL_ID, command.globalIdsVanGebruikersMetRol());
	}

	@Test
	@DisplayName("""
			GIVEN globalId of vestiging
			WHEN clearVestigingCache is called
			THEN cacheService will be called to clear
			""")
	void clearVestigingCache() {
		vestigingenRestController.clearVestigingCache(VESTIGING_ID);
		verify(cacheService).clearVestigingCache(VESTIGING_ID);
	}

	@Test
	@DisplayName("""
			GIVEN globalId of vestiging
			WHEN getAdditionalRoles is called
			THEN adminDomainService will be called to get additional roles
			""")
	void getAdditionalRoles() {
		var adminDomain = new AdminDomainObject();
		var dn = Dn.of("1");
		when(applicationProperties.createAdminDomainDn("1")).thenReturn(dn);
		when(adminDomainService.findAdminDomainByDnWithRoles(any(Dn.class))).thenReturn(Optional.of(adminDomain));

		vestigingenRestController.getAdditionalRoles(VESTIGING_ID);

		verify(adminDomainService).getAdditionalRoles(adminDomain.getRoles());
	}

	@Test
	@DisplayName("""
			GIVEN globalId of vestiging, globalId of role and valid command
			WHEN saveAdditionalRoles is called
			THEN adminDomainService will be called to add and remove roles
			""")
	void saveAdditionalRoles() {
		var adminDomain = new AdminDomainObject();
		var dn = Dn.of("1");
		when(applicationProperties.createAdminDomainDn("1")).thenReturn(dn);
		when(adminDomainService.findAdminDomainByDnWithRoles(any(Dn.class))).thenReturn(Optional.of(adminDomain));

		var roleToAdd = new RoleObject();
		roleToAdd.setHasRole(true);
		var roleToRemove = new RoleObject();
		roleToRemove.setHasRole(false);
		var roles = List.of(roleToAdd, roleToRemove);

		vestigingenRestController.saveAdditionalRoles(VESTIGING_ID, roles);

		verify(adminDomainService).addAndRemoveRoles(adminDomain, List.of(roleToAdd), List.of(roleToRemove));
	}

}
