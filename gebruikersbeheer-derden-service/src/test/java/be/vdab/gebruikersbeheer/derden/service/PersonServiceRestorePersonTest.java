package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.service.cache.CacheService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsRequest;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsRequestService;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceRestorePersonTest {

	private static final Dn PERSON_DN = Dn.of("erglobalid=123,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");

	@InjectMocks
	PersonServiceImpl personService;

	@Mock
	IsimWsClient isimWsClient;

	@Mock
	IsimWsSession isimWsSession;

	@Mock
	PersonObject personObject;

	@Mock
	IsimWsRequest isimWsRequest;

	@Mock
	IsimWsRequestService isimWsRequestService;

	@Mock
	CacheService cacheService;

	@Mock
	IsimUserContextManager isimUserContextManager;

	@Test
	void restorePerson() {
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);

		when(personObject.getDn()).thenReturn(PERSON_DN);
		when(isimWsClient.restorePerson(isimWsSession, PERSON_DN)).thenReturn(Optional.of(isimWsRequest));
		when(isimWsRequestService.waitUntilFinished(isimWsSession, isimWsRequest)).thenReturn(isimWsRequest);
		when(isimWsRequest.hasCompletedWithoutFailure()).thenReturn(true);

		personService.restorePerson(personObject);

		verify(isimWsClient).restorePerson(isimWsSession, PERSON_DN);
		verify(cacheService).deleteFromPersonCaches(personObject);
	}
}
