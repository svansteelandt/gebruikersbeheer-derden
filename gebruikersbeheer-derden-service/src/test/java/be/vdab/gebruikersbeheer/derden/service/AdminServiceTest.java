package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.CacheNames;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsAdminDomain;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
	
	static final String globalIdReal = "7797760088491035196";
	static final String globalIdVirtual = "789";

	@InjectMocks
	AdminServiceImpl adminService;

	@Mock
	IsimWsClient isimWsClient;
	@Mock
	IsimWsAdminDomain isimWsAdminDomain;
	@Mock
	IsimWsSession isimWsSession;
	@Mock
	CacheManager cacheManager;
	@Mock
	Cache cache;
	@Mock
	IsimUserContextManager isimUserContextManager;

	@Test
	void createNewAdmin() {
		AdminDomainObject adminDomainObject = createAdminDomainObjectWith2Admins(globalIdVirtual, globalIdReal);

		PersonObject person = createPersonObject("123456", false);
		when(isimWsClient.getAdminDomainByDn(isimWsSession, adminDomainObject.getDn())).thenReturn(isimWsAdminDomain);
		when(isimWsAdminDomain.addAdministrator(person.getDn())).thenReturn(isimWsAdminDomain);
		when(cacheManager.getCache(CacheNames.CACHE_ADMINDOMAIN)).thenReturn(cache);
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);

		adminService.createNewAdmin(adminDomainObject, person);

		verify(isimWsClient).getAdminDomainByDn(isimWsSession, adminDomainObject.getDn());
		verify(isimWsAdminDomain).addAdministrator(person.getDn());
		verify(isimWsClient).updateAdminDomain(isimWsSession, isimWsAdminDomain, isimWsAdminDomain);
	}


	@Test
	void deleteAdmin() {
		AdminDomainObject adminDomainObject = createAdminDomainObjectWith2Admins(globalIdVirtual, globalIdReal);
		adminDomainObject.getAdministrators().removeFirst();
		PersonObject person = createPersonObject("123456", false);
		when(isimWsClient.getAdminDomainByDn(isimWsSession, adminDomainObject.getDn())).thenReturn(isimWsAdminDomain);
		when(isimWsAdminDomain.deleteAdministrator(person.getDn())).thenReturn(isimWsAdminDomain);
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);

		adminService.deleteAdmin(adminDomainObject, person);

		verify(isimWsClient).getAdminDomainByDn(isimWsSession, adminDomainObject.getDn());
		verify(isimWsAdminDomain).deleteAdministrator(person.getDn());
		verify(isimWsClient).updateAdminDomain(isimWsSession, isimWsAdminDomain, isimWsAdminDomain);
	}

	@Test
	void deleteFromAdminDomainCache() {
		when(cacheManager.getCache(CacheNames.CACHE_ADMINDOMAIN)).thenReturn(cache);

		adminService.deleteFromAdminDomainCache("1");

		verify(cache).evict("1");
	}

	private AdminDomainObject createAdminDomainObjectWith2Admins(String globalIdVirtual, String globalIdReal) {
		PersonObject personObjectVirtual = createPersonObject(globalIdVirtual, true);
		PersonObject personObjectReal = createPersonObject(globalIdReal, false);
		AdminDomainObject adminDomainObject = new AdminDomainObject();
		adminDomainObject.addAdministrator(personObjectVirtual);
		adminDomainObject.addAdministrator(personObjectReal);
		RoleObject roleObject = new RoleObject();
		roleObject.setAdminRole(true);
		adminDomainObject.setRoles(List.of(roleObject));
		return adminDomainObject;
	}


	private PersonObject createPersonObject(String globalId, boolean isVirtual) {
		Dn dnObject = Dn.of("erglobalid=" + globalId + ",ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");
		PersonObject personObject = new PersonObject();
		personObject.setVirtualAccount(isVirtual);
		personObject.setDn(dnObject);
		return personObject;
	}
}