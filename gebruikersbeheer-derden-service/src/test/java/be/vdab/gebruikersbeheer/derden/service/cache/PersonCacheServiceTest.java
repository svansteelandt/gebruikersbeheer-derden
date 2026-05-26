package be.vdab.gebruikersbeheer.derden.service.cache;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.service.AdminService;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonCacheServiceTest {

	static final String VESTIGING_ID = "1";
	static final String PERSON_ID = "2";

	@InjectMocks
	PersonCacheServiceImpl personCacheService;

	@Mock
	AdminService adminService;
	@Mock
	PersonService personService;
	@Mock
	CacheService cacheService;
	@Mock
	ApplicationProperties applicationProperties;

	@Test
	void clearVestigingCache() {
		var personDn = Dn.of("erglobalid=" + PERSON_ID + ",");
		var vestigingDn = Dn.of(VESTIGING_ID);
		when(applicationProperties.createAdminDomainDn(VESTIGING_ID)).thenReturn(vestigingDn);
		when(personService.findPersonsFromOrganization(vestigingDn)).thenReturn(List.of(personDn));

		personCacheService.clearVestigingCache(VESTIGING_ID);

		verify(adminService).deleteFromAdminDomainCache(vestigingDn);
		verify(cacheService).deleteFromPersonCaches(personDn);
	}
}