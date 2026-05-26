package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainSearch;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.domain.VestigingenZoekResultaat;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;

import java.util.List;
import java.util.Optional;

public interface AdminDomainService {

	Optional<AdminDomainObject> findAdminDomainByDn(Dn dn);

	Optional<AdminDomainObject> findAdminDomainByOrgId(String orgId);

	Optional<AdminDomainObject> findAdminDomainByOrgIdWithRoles(String orgId);

	List<String> getGlobalIdsFromNonVirtualAdminsFor(String orgId);

	Optional<AdminDomainObject> findAdminDomainByDnWithRoles(Dn dn);

	List<AdminDomainObject> findAdminDomainsForAdministrator(Dn adminDn);

	List<AdminDomainObject> findAdminDomainsForAdministrator(Dn adminDn, List<String> attributes);

	List<AdminDomainObject> findAdminDomainByIkps(List<String> ikpIds);

	Optional<AdminDomainObject> findAdminDomainByIkp(Ikp ikp);

	boolean existsAdminDomain(Ikp ikp);

	Optional<AdminDomainObject> findAdminDomainByName(String name);

	VestigingenZoekResultaat findAdminDomainBySearchCriteria(AdminDomainSearch adminDomainSearch);

	List<RoleObject> getAdditionalRoles(List<RoleObject> roleObjects);

	void addAndRemoveRoles(AdminDomainObject adminDomainObject, List<RoleObject> adds, List<RoleObject> deletes);
}
