package be.vdab.gebruikersbeheer.derden.web.internal.v1;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.AdminService;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.derden.service.cache.CacheService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisatiesControllerTest {

	@InjectMocks
	OrganisatiesController organisatiesController;

	@Mock
	PersonService personService;

	@Mock
	AdminService adminService;

	@Mock
	AdminDomainService adminDomainService;

	@Mock
	CacheService cacheService;

	@Mock
	ApplicationProperties applicationProperties;
	@Mock
	AdminDomainObject adminDomainObject;
	@Mock
	Ikp ikp;


	@Test
	void clearOrganizationFromCache() {
		Dn dn = new Dn("dn");
		when(applicationProperties.createAdminDomainDn("o")).thenReturn(dn);
		when(adminDomainObject.getIkp()).thenReturn(ikp);
		when(adminDomainService.findAdminDomainByDn(dn)).thenReturn(Optional.of(adminDomainObject));
		organisatiesController.clearOrganizationFromCache("o");

		verify(adminService).deleteFromAdminDomainCache(dn);
		verify(adminService).deleteFromAdminDomainCache(ikp);
	}
}