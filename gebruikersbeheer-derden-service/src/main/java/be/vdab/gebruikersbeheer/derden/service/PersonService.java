package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.RoleNotFoundException;
import be.vdab.gebruikersbeheer.derden.web.internal.v1.Insz;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;

import java.util.List;
import java.util.Optional;

public interface PersonService {

	List<Dn> findPersonsFromOrganization(Dn organizationDn);

	List<PersonObject> findPersonsFromOrganization(AdminDomainObject adminDomain);

	List<PersonObject> findNonMlpOpleidingPersonsFromOrganization(AdminDomainObject adminDomain);

	Optional<PersonObject> findPersonByDn(Dn personDn, AdminDomainObject adminDomainObject);

	Optional<PersonObject> findPersonWithRolesByGlobalId(String personGlobalId);

	PersonObject findPersonByGebruikersnaam(String gebruikersnaam) throws PersonNotFoundException;

	PersonObject findPersonByGebruikersnaam(String gebruikersnaam, List<String> attributes) throws PersonNotFoundException;

	List<PersonObject> findPersons(FindPersonQuery query);

	Dn insertPerson(AdminDomainObject adminDomainObject, PersonObject personObject, boolean addToCache);

	void updatePerson(PersonObject personObject);

	boolean deletePerson(AdminDomainObject adminDomainObject, PersonObject personObject) throws MinimumAdminsReachedException;

	boolean restorePerson(PersonObject personObject);

	List<PersonObject> findPersonsFromOrganizationDnWithRoleWithPending(Dn organizationDn, RoleObject roleObject);

	boolean rrnExists(String rrn, Dn distinguishedName);

	List<PersonObject> findPersonsInPrullenbakForIkpNummer(Ikp ikpNummer);

	Dn getPersonDnInPrullenbakForRrnAndIkpNummer(String rrn, Ikp ikpNummer);

	boolean changeLoginMethod(String personId, String loginMethod);

	void toevoegenRol(String gebruikersnaam, String rol) throws PersonNotFoundException, RoleNotFoundException;

	void verwijderenRol(String gebruikersnaam, String rol) throws PersonNotFoundException, RoleNotFoundException;

	void updatePersonCaches(Dn personDn, AdminDomainObject adminDomain);

	String getGlobalIdFromPersonBy(String orgId, Insz insz);

	List<PersonObject> getDerdeProfilesBy(String rrn);
}
