package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsRequest;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsRequestService;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceChangePersonRoleTest {

	private static final Dn PERSON_DN = Dn.of("erglobalid=123,ou=0,ou=persons,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");

	private static final Dn ROLE_DN = Dn.of("erglobalid=456,ou=roles,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");

	@InjectMocks
	RoleServiceImpl roleService;

	@Mock
	IsimWsClient isimWsClient;

	@Mock
	IsimWsSession isimWsSession;

	@Mock
	PersonObject personObject;

	@Mock
	RoleObject roleObject;

	@Mock
	IsimWsRequest request;

	@Mock
	IsimWsRequestService isimWsRequestService;

	@Mock
	CacheManager cacheManager;

	@Mock
	Cache cachePersons;
	@Mock
	IsimUserContextManager isimUserContextManager;

	@BeforeEach
	void init() {
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(personObject.getDn()).thenReturn(PERSON_DN);
		when(roleObject.getDn()).thenReturn(ROLE_DN);
		when(roleObject.getRoleName()).thenReturn("xxx");

		when(isimWsRequestService.waitUntilProcessingHasStarted(isimWsSession, request)).thenReturn(request);
		when(cacheManager.getCache(anyString())).thenReturn(cachePersons);
	}

	@Test
	void addRole() {
		when(isimWsClient.addRoleToPerson(isimWsSession, PERSON_DN, ROLE_DN)).thenReturn(Optional.of(request));

		roleService.changePersonRole(personObject, roleObject, true);

		verify(isimWsClient).addRoleToPerson(isimWsSession, PERSON_DN, ROLE_DN);
		verify(cachePersons).evict(PERSON_DN);
	}

	@Test
	void removeRole() {
		when(isimWsClient.removeRoleFromPerson(isimWsSession, PERSON_DN, ROLE_DN)).thenReturn(Optional.of(request));
		roleService.changePersonRole(personObject, roleObject, false);

		verify(isimWsClient).removeRoleFromPerson(isimWsSession, PERSON_DN, ROLE_DN);
		verify(cachePersons).evict(PERSON_DN);
	}
}
