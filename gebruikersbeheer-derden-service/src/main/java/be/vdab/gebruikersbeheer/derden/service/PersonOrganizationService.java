package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;

import java.util.List;

public interface PersonOrganizationService {
	boolean existsPersonInOrganization(Dn personDn, Dn adminDomainDn);

	List<PersonObject> findPersonsByDn(List<Dn> dns);
}
