package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.domain.IsimPerson;

import java.util.List;
import java.util.Optional;

public interface RoleService {
	
	List<RoleObject> findRoles();

	boolean isRoleInCache(Dn roleDn);

	void addRoleToCache(RoleObject roleObject);

	Optional<RoleObject> findRoleByDN(Dn distinguishedName);

	Optional<RoleObject> findRoleByGlobalId(String globalId);

	List<RoleObject> findRolesByGlobalId(List<String> globalIds);

	void changePersonRole(IsimWsSession isimWsSession, PersonObject person, RoleObject roleObject, boolean attach);

	Optional<RoleObject> findByRoleName(String roleName);

	RoleObject findAdminRole();

	void addAndRemoveRoles(PersonObject personObject, AdminDomainObject adminDomainObject);

	void changePersonRoleChangeList(List<PersonObject> personObjects, RoleObject roleObject, AdminDomainObject adminDomainObject);

	void changePersonRoles(IsimWsSession isimWsSession, IsimPerson oldPerson, PersonObject newPerson, List<RoleObject> rolesToAdd, List<RoleObject> rolesToDelete);

	void changePersonRole(PersonObject person, RoleObject roleObject, boolean attach);
	
	int getAantalAdmins(AdminDomainObject adminDomainObject);

	boolean personHasRole(IsimPerson person, RoleObject roleObject);
}
