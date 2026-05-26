package be.vdab.gebruikersbeheer.derden.service.cache;

import be.vdab.gebruikersbeheer.derden.config.CacheNames;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

	private final CacheManager cacheManager;

	@Override
	public void addPersonToPersonCaches(PersonObject personObject) {
		Cache cachePersons = this.cacheManager.getCache(CacheNames.CACHE_PERSONS);
		if (cachePersons != null) {
			cachePersons.put(personObject.getDn(), personObject);
		}
	}

	@Override
	public void updatePersonInCache(Dn personDn, PersonObject personObjectNew) {
		Cache cachePersons = this.cacheManager.getCache(CacheNames.CACHE_PERSONS);

		if (cachePersons != null) {
			PersonObject personFromCache = cachePersons.get(personDn, PersonObject.class);

			if (personFromCache != null) {
				log.trace("Person {} in cache", personFromCache.getUserId());
			} else {
				log.trace("Person {} NOT in cache", personObjectNew.getUserId());

				// gebruiker zit niet in cache, geen probleem, niets te updaten
				return;
			}

			cachePersons.evict(personDn);

			log.trace("Add person {} to cache", personObjectNew.getUserId());

			if (personObjectNew.getRoles().isEmpty()) {
				log.debug("No roles");
			}

			if (log.isDebugEnabled()) {
				personObjectNew.getRoles().forEach(r -> log.debug("Role {} hasrole: {} changed: {}", r.getRoleName(), r.getHasRole(), r.isChanged()));
			}

			cachePersons.put(personDn, personObjectNew);
		}
	}

	@Override
	public void deleteFromPersonCaches(Dn personDn) {
		Cache cachePersons = this.cacheManager.getCache(CacheNames.CACHE_PERSONS);

		if (cachePersons != null) {
			cachePersons.evict(personDn);
		}
	}

	@Override
	public void deleteFromPersonCaches(PersonObject personObject) {
		Cache cachePersons = this.cacheManager.getCache(CacheNames.CACHE_PERSONS);
		if (cachePersons != null) {
			cachePersons.evict(personObject.getDn());
		}
	}
}
