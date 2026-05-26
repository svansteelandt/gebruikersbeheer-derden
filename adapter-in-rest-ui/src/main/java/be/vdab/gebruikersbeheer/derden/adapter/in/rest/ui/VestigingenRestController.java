package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper.VestigingMapper;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingSummaryDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingenZoekResultaatDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.WijsGebruikersToeAanRolCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.search.LimitedSearch;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainSearch;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.domain.VestigingenZoekResultaat;
import be.vdab.gebruikersbeheer.derden.exception.MaximumAantalAdminsOverschredenException;
import be.vdab.gebruikersbeheer.derden.exception.MinimumAantalAdminsNietGehaaldException;
import be.vdab.gebruikersbeheer.derden.exception.SearchCriteriaVerplichtException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.AssignUsersToRolService;
import be.vdab.gebruikersbeheer.derden.service.cache.PersonCacheService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ui/vestigingen")
@Slf4j
@RequiredArgsConstructor
public class VestigingenRestController {
	private final ApplicationProperties applicationProperties;
	private final AdminDomainService adminDomainService;
	private final VestigingMapper vestigingMapper;
	private final AssignUsersToRolService assignUsersToRolService;
	private final PersonCacheService cacheService;

	@GetMapping()
	public VestigingenZoekResultaatDto zoek(@RequestParam(required = false) @Size(max = 150, min = 2) String vestigingNaam,
	                                        @RequestParam(required = false) @Size(max = 150) String straatNaam,
	                                        @RequestParam(required = false) @Size(max = 150) String gemeente,
	                                        @RequestParam(required = false) @Size(max = 20) String postcode,
	                                        @RequestParam(required = false) @Size(max = 10) String ikpHoofdzetelNummer,
	                                        @RequestParam(required = false) @Size(max = 3) String ikpVestigingNummer,
	                                        @RequestParam(required = false) @Size(max = 20) String kboNummer,
	                                        @RequestParam(required = false) Long oeNummer) throws SearchCriteriaVerplichtException {
		var searchCommand = new AdminDomainSearch();
		searchCommand.setName(vestigingNaam);
		searchCommand.setStreet(straatNaam);
		searchCommand.setCity(gemeente);
		searchCommand.setPostalcode(postcode);
		searchCommand.setIkp(ikpHoofdzetelNummer);
		searchCommand.setIkpEnd(ikpVestigingNummer);
		searchCommand.setKboNummer(kboNummer);
		if (oeNummer != null) {
			searchCommand.setOe(oeNummer.toString());
		}

		if (!searchCommand.hasSearchCriteria()) {
			throw new SearchCriteriaVerplichtException();
		}

		var zoekResultaat = new LimitedSearch<>(searchCommand, AdminDomainSearch::setLimit)
				.setLimit(applicationProperties.getUiSearchLimit())
				.execute(adminDomainService::findAdminDomainBySearchCriteria, VestigingenZoekResultaat::vestigingen);

		List<VestigingSummaryDto> vestigingen = vestigingMapper.map(zoekResultaat.limitedList());
		return new VestigingenZoekResultaatDto(vestigingen, zoekResultaat.tooManyResults());
	}

	@GetMapping("/{globalId}")
	public VestigingDto getVestiging(@PathVariable() String globalId) {
		var organizationDn = applicationProperties.createAdminDomainDn(globalId);

		//TODO : bekijken nadat volledige omzetting is gebeurd : admin domain altijd ophalen ZONDER de personobjecten op te halen van de administrators
		var adminDomainObject = adminDomainService.findAdminDomainByDnWithRoles(organizationDn);

		if (adminDomainObject.isEmpty()) {
			throw new VestigingNietGevondenException(globalId);
		}
		return vestigingMapper.map(adminDomainObject.get());
	}

	@GetMapping("/{globalId}/additional-roles")
	@PreAuthorize("@SecurityExpressions.canChangeRights()")
	public List<RoleObject> getAdditionalRoles(@PathVariable String globalId) {
		AdminDomainObject adminDomainObject = adminDomainService
				.findAdminDomainByDnWithRoles(applicationProperties.createAdminDomainDn(globalId)).orElseThrow(() -> new VestigingNietGevondenException(globalId));
		return adminDomainService.getAdditionalRoles(adminDomainObject.getRoles());
	}

	@PostMapping("/{globalId}/additional-roles")
	@PreAuthorize("@SecurityExpressions.canChangeRights()")
	public void saveAdditionalRoles(@PathVariable String globalId, @RequestBody List<RoleObject> roles) {
		AdminDomainObject adminDomainObject = adminDomainService.findAdminDomainByDnWithRoles(applicationProperties.createAdminDomainDn(globalId))
				.orElseThrow(() -> new VestigingNietGevondenException(globalId));

		List<RoleObject> adds = new ArrayList<>();
		List<RoleObject> deletes = new ArrayList<>();
		List<RoleObject> adminRoles = adminDomainObject.getRoles();

		roles.stream()
				.forEach(role -> divideBetweenAddsAndDeletes(adds, deletes, adminRoles, role));

		this.adminDomainService.addAndRemoveRoles(adminDomainObject, adds, deletes);
	}


	@DeleteMapping("/{globalId}/cache")
	@PreAuthorize("@SecurityExpressions.isBeheerderDerden()")
	public void clearVestigingCache(@PathVariable() String globalId) {
		this.cacheService.clearVestigingCache(globalId);
	}

	@PutMapping("/{globalIdOfOrganisation}/rollen/{globalIdOfRol}/gebruikers")
	@PreAuthorize("@SecurityExpressions.isBeheerderDerden()")
	public void wijzigAanRolToegewezenGebruikers(@PathVariable() String globalIdOfOrganisation,
	                                             @PathVariable() String globalIdOfRol,
	                                             @Valid @RequestBody WijsGebruikersToeAanRolCommand wijsGebruikersToeAanRolCommand) throws MaximumAantalAdminsOverschredenException, MinimumAantalAdminsNietGehaaldException {
		this.assignUsersToRolService.assign(globalIdOfOrganisation, globalIdOfRol, wijsGebruikersToeAanRolCommand.globalIdsVanGebruikersMetRol());
	}

	private void divideBetweenAddsAndDeletes(List<RoleObject> adds, List<RoleObject> deletes, List<RoleObject> adminRoles, RoleObject role) {
		if (role.getHasRole()) {
			if (!adminRoles.contains(role)) {
				adds.add(role);
			}
		} else {
			deletes.add(role);
		}
	}
}
