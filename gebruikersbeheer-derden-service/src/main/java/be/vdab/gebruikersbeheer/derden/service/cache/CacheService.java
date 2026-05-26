package be.vdab.gebruikersbeheer.derden.service.cache;

import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;

public interface CacheService {

	void addPersonToPersonCaches(PersonObject personObject);

	void updatePersonInCache(Dn personDn, PersonObject personObjectNew);

	void deleteFromPersonCaches(Dn personDn);

	void deleteFromPersonCaches(PersonObject personObject);
}
