package be.vdab.gebruikersbeheer.derden.web.admin;

import be.vdab.gebruikersbeheer.derden.config.CacheNames;
import be.vdab.gebruikersbeheer.derden.service.FindPersonQuery;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.ArrayList;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GebruikersbeheerCacheControllerTest {

	@Mock
	CacheManager cacheManager;

	@Mock
	PersonService personService;

	@Mock
	Cache cache;

	@InjectMocks
	GebruikersbeheerCacheController gebruikersbeheerCacheController;

	@Test
	void clearCacheUser(){
		String username= "AVHOYE";
		var query = FindPersonQuery.builder().gebruikersnaam(username).build();

		when(cacheManager.getCache(CacheNames.CACHE_PERSONS)).thenReturn(cache);
		when(personService.findPersons(query)).thenReturn(new ArrayList<>());

		gebruikersbeheerCacheController.clearCacheForUsername(username);

		verify(personService).findPersons(query);
	}
}
