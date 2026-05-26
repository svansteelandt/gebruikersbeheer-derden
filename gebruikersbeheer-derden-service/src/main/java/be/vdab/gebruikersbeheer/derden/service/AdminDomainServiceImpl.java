package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.config.CacheNames;
import be.vdab.gebruikersbeheer.derden.converter.AdminDomainConverter;
import be.vdab.gebruikersbeheer.derden.core.CodesPort;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainSearch;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.domain.VestigingenZoekResultaat;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextHolder;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.util.LogSanitizer;
import be.vdab.gebruikersbeheer.derden.util.SecurityUtils;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.exception.OrganizationNotFoundException;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsAdminDomain;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapAdminDomain;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.LdapFilter;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.OrFilterBuilder;
import be.vdab.iam.oidc.authentication.principal.VdabPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames.DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_EXCL_ROLES;
import static be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames.DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_INCL_ROLES;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminDomainServiceImpl implements AdminDomainService {

	private final IsimLdapManager isimLdapManager;
	private final IsimWsClient isimWsClient;
	private final AdminDomainConverter adminDomainConverter;
	private final SecurityUtils securityUtils;
	private final RoleService roleService;
	private final CodesPort codesPort;
	private final ApplicationProperties applicationProperties;
	private final AdminService adminService;
	private final IsimUserContextManager isimUserContextManager;

	@Override
	public Optional<AdminDomainObject> findAdminDomainByDn(Dn dn) {
		log.trace("findAdminDomainByDn dn: {}", dn);
		return isimLdapManager.getAdminDomainByDn(dn, DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_INCL_ROLES)
				.map(adminDomain -> adminDomainConverter.convert(adminDomain, false));
	}

	@Override
	public List<AdminDomainObject> findAdminDomainByIkps(List<String> ikpIds) {
		log.trace("findAdminDomainByIkps");

		OrFilterBuilder filterBuilder = LdapFilter.orFilter();
		ikpIds.stream()
				.map(ikp -> LdapFilter.equalityFilter(IsimAttributeNames.ATTR_IKP, ikp))
				.forEach(filterBuilder::add);

		List<IsimLdapAdminDomain> adminDomains = isimLdapManager.getAdminDomainsByFilter(filterBuilder.build(), DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_EXCL_ROLES);
		return adminDomainConverter.convertList(adminDomains);
	}

	@Override
	@Cacheable(cacheNames = CacheNames.CACHE_ADMINDOMAIN)
	public Optional<AdminDomainObject> findAdminDomainByIkp(Ikp ikp) {
		log.trace("findAdminDomainByIkp ikp: {}", ikp);

		return isimLdapManager.getAdminDomainByIkp(ikp, DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_INCL_ROLES)
				.map(adminDomain -> adminDomainConverter.convert(adminDomain, true));
	}

	@Override
	public boolean existsAdminDomain(Ikp ikp) {
		return isimLdapManager.getAdminDomainByIkp(ikp, DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_INCL_ROLES).isPresent();
	}

	@Override
	@Cacheable(cacheNames = CacheNames.CACHE_ADMINDOMAIN)
	public Optional<AdminDomainObject> findAdminDomainByName(String name) {
		log.trace("findAdminDomainByName name: {}", name);
		return isimLdapManager.getAdminDomainByName(name, DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_INCL_ROLES)
				.map(adminDomain -> adminDomainConverter.convert(adminDomain, true));
	}

	@Override
	public Optional<AdminDomainObject> findAdminDomainByOrgId(String orgId) {
		log.trace("findAdminDomainByOrgId orgId: {}", orgId);
		return isimLdapManager.getAdminDomainByOrgId(orgId, DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_INCL_ROLES).map(adminDomainConverter::convert);
	}

	@Override
	public Optional<AdminDomainObject> findAdminDomainByOrgIdWithRoles(String orgId) {
		log.trace("findAdminDomainByOrgId orgId: {}", orgId);
		return isimLdapManager.getAdminDomainByOrgId(orgId, DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_INCL_ROLES)
				.map(adminDomain -> adminDomainConverter.convert(adminDomain, true));
	}

	@Override
	public List<String> getGlobalIdsFromNonVirtualAdminsFor(String orgId) {
		log.trace("getNonVirtualAdminsFor orgId: {}", orgId);
		return findAdminDomainByOrgId(orgId)
				.orElseThrow(() -> new OrganizationNotFoundException(orgId))
				.getAdministrators().stream()
				.filter(po -> !po.isVirtualAccount())
				.map(PersonObject::getDn)
				.map(Dn::getGlobalId)
				.collect(Collectors.toList());
	}

	@Override
	@Cacheable(cacheNames = CacheNames.CACHE_ADMINDOMAIN)
	public Optional<AdminDomainObject> findAdminDomainByDnWithRoles(Dn dn) {
		log.trace("findAdminDomainByDnWithRoles dn: {}", dn);
		return isimLdapManager.getAdminDomainByDn(dn, DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_INCL_ROLES)
				.map(adminDomain -> adminDomainConverter.convert(adminDomain, true));
	}

	@Override
	@Cacheable(cacheNames = CacheNames.CACHE_ADMINDOMAINS)
	public List<AdminDomainObject> findAdminDomainsForAdministrator(Dn adminDn) {
		return findAdminDomainsForAdministrator(adminDn, DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_INCL_ROLES);
	}

	@Override
	@Cacheable(cacheNames = CacheNames.CACHE_ADMINDOMAINS)
	public List<AdminDomainObject> findAdminDomainsForAdministrator(Dn adminDn, List<String> attributes) {
		return isimLdapManager.getAdminDomainsByFilter(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_ADMINSTRATOR, adminDn.getValue()), attributes).stream()
				.flatMap(adminDomain -> addSubDomains(adminDomain, attributes).stream())
				.map(adminDomain -> adminDomainConverter.convert(adminDomain, false)).collect(Collectors.toList());
	}

	private List<IsimLdapAdminDomain> addSubDomains(IsimLdapAdminDomain adminDomain, List<String> attributes) {
		List<IsimLdapAdminDomain> subDomains = isimLdapManager.getAdminDomainsByFilter(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_PARENT, adminDomain.getDn().getValue()), attributes);

		List<IsimLdapAdminDomain> result = new ArrayList<>();
		result.add(adminDomain);

		if (subDomains != null) {
			result.addAll(subDomains);
		}

		return result;
	}

	@Override
	public VestigingenZoekResultaat findAdminDomainBySearchCriteria(AdminDomainSearch adminDomainSearch) {
		log.trace("findAdminDomainBySearchCriteria AdmindomainSearch: {}", LogSanitizer.sanitize(adminDomainSearch.toString()));
		boolean zoekCriteriaZijnTeAlgemeen = false;
		List<AdminDomainObject> adminDomains = adminDomainConverter.convertList(isimLdapManager.getAdminDomainsByFilter(adminDomainSearch.ldapFilter(), DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_EXCL_ROLES, adminDomainSearch.getLimit()), true, true);

		if (adminDomains.size() > applicationProperties.getDaoMaxRecords()) {
			zoekCriteriaZijnTeAlgemeen = true;
		}

		List<AdminDomainObject> returnAdminDomains = new ArrayList<>();
		for (AdminDomainObject adminDomainObject : adminDomains) {
			if (adminDomainSearch.getIkp() == null || adminDomainSearch.getIkp().isEmpty() || (adminDomainObject.getIkp().toString().length() == (adminDomainSearch.getIkp().length() + 3))) {
				returnAdminDomains.add(adminDomainObject);
			}
		}

		return new VestigingenZoekResultaat(returnAdminDomains, zoekCriteriaZijnTeAlgemeen);
	}

	@Override
	public void addAndRemoveRoles(AdminDomainObject adminDomainObject, List<RoleObject> addRoles, List<RoleObject> deleteRoles) {
		IsimWsSession isimWsSession = isimUserContextManager.getSession();
		IsimWsAdminDomain adminDomainOld = isimWsClient.getAdminDomainByDn(isimWsSession, adminDomainObject.getDn());

		Set<Dn> rolDns = new HashSet<>(adminDomainOld.getRollen());
		deleteRoles.forEach(role -> rolDns.remove(role.getDn()));
		addRoles.forEach(role -> rolDns.add(role.getDn()));
		IsimWsAdminDomain adminDomainNew = adminDomainOld.setRollen(rolDns);

		isimWsClient.updateAdminDomain(isimWsSession, adminDomainOld, adminDomainNew);

		adminService.deleteFromAdminDomainCache(adminDomainObject.getDn());
	}

	@Override
	@Nullable
	public List<RoleObject> getAdditionalRoles(List<RoleObject> adminRoles) {
		List<RoleObject> extranetRoles = getAdditionalRoles();
		extranetRoles.stream()
				.filter(adminRoles::contains)
				.forEach(r -> r.setHasRole(true));

		return extranetRoles;
	}

	private List<RoleObject> getAdditionalRoles() {
		List<RoleObject> roles = this.roleService.findRoles();
		List<String> defaultRolesFromSamakk = this.codesPort.getAdditionalTimRollen();

		Stream<RoleObject> additionalRoles = roles.stream()
				.filter(this::isAdditionalRole)
				.filter(this::hasTagDerdeIntern)
				.filter(r -> isNotDefaultAssignedToOrganization(defaultRolesFromSamakk, r));

		if (!principalHasKioskAdminRole()) {
			additionalRoles = additionalRoles.filter(r -> !this.isKioskPartner(r));
		}

		if (ingelogdeGebruikerDoesNotHaveBeheerderRole() && ingelogdeGebruikerHasExpertRole()) {
			additionalRoles = additionalRoles.filter(this::isApprovedByPrincipalOrDoesNotNeedApproval);
		}

		if (ingelogdeGebruikerDoesNotHaveBeheerderRole() && !ingelogdeGebruikerHasExpertRole()) {
			additionalRoles = additionalRoles.filter(r -> !this.isExtranetRole(r));
		}

		return additionalRoles.collect(Collectors.toList());
	}

	private boolean principalHasKioskAdminRole() {
		return getVdabPrincipal()
				.map(principal -> principal.hasRole(RoleNames.ROL_KIOSK_ADMIN))
				.orElse(false);
	}

	private boolean isAdditionalRole(RoleObject r) {
		return isExtranetRole(r) || isKioskPartner(r);
	}

	private boolean isKioskPartner(RoleObject role) {
		return "AR-KIOSK-PARTNER".equals(role.getRoleName());
	}

	private boolean hasTagDerdeIntern(RoleObject r) {
		return r.getTags() != null && r.getTags().contains("DerdeIntern");
	}

	private boolean isNotDefaultAssignedToOrganization(List<String> alreadyAssignedRoles, RoleObject r) {
		return !alreadyAssignedRoles.contains(r.getRoleName());
	}

	private boolean isExtranetRole(RoleObject r) {
		return r.getRoleName().startsWith("AR-EXTRANET-");
	}

	private boolean ingelogdeGebruikerHasExpertRole() {
		return getVdabPrincipal()
				.map(principal -> principal.hasRole(RoleNames.ROL_EXTRANET_EXPERT))
				.orElse(false);
	}

	private boolean ingelogdeGebruikerDoesNotHaveBeheerderRole() {
		return getVdabPrincipal()
				.map(principal -> !principal.hasRole(RoleNames.ROL_BEHEERDERS_EXTRANET))
				.orElse(true);
	}

	private boolean isApprovedByPrincipalOrDoesNotNeedApproval(RoleObject role) {
		return !role.isNeedsApproval() || approvedByPrincipal(role);
	}

	private boolean approvedByPrincipal(RoleObject role) {
		return role.isNeedsApproval() && personOwnsRole(role);
	}

	private boolean personOwnsRole(RoleObject role) {
		Dn personDn = IsimUserContextHolder.getContext().getPersonDn();
		if (personDn == null) {
			throw new IllegalStateException("Session should have a principal.");
		}

		return role.getOwners() != null && role.getOwners().contains(personDn);
	}

	private Optional<VdabPrincipal> getVdabPrincipal() {
		return (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof VdabPrincipal principal)
				? Optional.of(principal)
				: Optional.empty();
	}
}
