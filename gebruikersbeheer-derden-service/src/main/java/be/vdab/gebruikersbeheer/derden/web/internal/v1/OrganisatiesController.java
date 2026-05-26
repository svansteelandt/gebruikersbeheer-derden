package be.vdab.gebruikersbeheer.derden.web.internal.v1;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.AdminService;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.derden.service.cache.CacheService;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.exception.OrganizationNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/internal/gebruikersbeheer/v1/organisaties", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrganisatiesController {

	private final AdminDomainService adminDomainService;
	private final AdminService adminService;
	private final PersonService personService;
	private final CacheService cacheService;
	private final ApplicationProperties applicationProperties;

	@Operation(summary = "Geeft lijst van globalIds van de admins voor dit orgId",
			description = "Geeft lijst van globalIds van de admins voor dit orgId",
			responses = {
					@ApiResponse(responseCode = "200", description = "Geeft lijst van globalIds van admins terug"),
					@ApiResponse(responseCode = "404", description = "OrgId bestaat niet"),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			})
	@GetMapping("{orgId}/admins")
	@PreAuthorize("@SecurityExpressions.isBeheerderDerden()")
	public List<String> getAdminsForOrganization(@PathVariable String orgId) {
		return adminDomainService.getGlobalIdsFromNonVirtualAdminsFor(orgId);
	}

	@Operation(summary = "Geeft de globalId van de opgegeven gebruiker van het opgegeven bedrijf.",
			description = "Geeft de globalId van de opgegeven gebruiker van het opgegeven bedrijf.",
			responses = {
					@ApiResponse(responseCode = "200", description = "Geeft de globald van persoon op basis van INSZ en orgId"),
					@ApiResponse(responseCode = "404", description = "OrgId/INSZ bestaat niet"),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			})
	@PostMapping("{orgId}/personen/globalId")
	@PreAuthorize("@SecurityExpressions.isBeheerderDerden()")
	public String getGlobalIdForPerson(@PathVariable String orgId, @Valid @RequestBody Insz insz) {
		return personService.getGlobalIdFromPersonBy(orgId, insz);
	}

	@Operation(summary = "Geeft lijst van rollen voor dit orgId",
			description = "Geeft lijst van rollen voor dit orgId",
			responses = {
					@ApiResponse(responseCode = "200", description = "Geeft lijst van rollen terug", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
					@ApiResponse(responseCode = "404", description = "OrgId bestaat niet"),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			})
	@GetMapping("{orgId}/rollen")
	@PreAuthorize("@SecurityExpressions.isBeheerderDerden()")
	public List<String> getRollenForOrganization(@PathVariable String orgId) {
		AdminDomainObject adminDomain = adminDomainService.findAdminDomainByOrgIdWithRoles(orgId).orElseThrow(() -> new OrganizationNotFoundException(orgId));
		return adminDomain.getRoles().stream()
				.map(RoleObject::getRoleName)
				.filter(roleName -> !roleName.equalsIgnoreCase(RoleNames.ROL_DOMAIN_ADMINS))
				.collect(Collectors.toList());
	}

	@Operation(summary = "Verwijdert organisatie uit de cache met dit orgId",
			description = "Verwijdert organisatie uit de cache met dit orgId",
			responses = {
					@ApiResponse(responseCode = "200", description = "Verwijdert organisatie uit de cache met dit orgId", content = @Content(schema = @Schema(implementation = ClearCacheResponse.class))),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			})
	@GetMapping("{orgId}/clearcache")
	public ClearCacheResponse clearOrganizationFromCache(@PathVariable String orgId) {
		Dn organizationDn = applicationProperties.createAdminDomainDn(orgId);

		Optional<AdminDomainObject> optionalAdminDomainObject = adminDomainService.findAdminDomainByDn(organizationDn);

		optionalAdminDomainObject.ifPresent(adminDomain -> adminService.deleteFromAdminDomainCache(adminDomain.getIkp()));

		adminService.deleteFromAdminDomainCache(organizationDn);

		List<Dn> personDns = this.personService.findPersonsFromOrganization(organizationDn);

		if (personDns != null) {
			personDns.forEach(cacheService::deleteFromPersonCaches);
		}

		return new ClearCacheResponse("ORGANIZATION_CACHE_CLEARED", "Organisatie verwijderd uit de cache");
	}

	private static class ClearCacheResponse {

		public final String key;
		public final String message;

		ClearCacheResponse(String key, String message) {
			this.key = key;
			this.message = message;
		}
	}
}
