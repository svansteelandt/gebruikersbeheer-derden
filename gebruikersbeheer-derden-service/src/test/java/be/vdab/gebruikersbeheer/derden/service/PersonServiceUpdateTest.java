package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.converter.PersonConverter;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.service.cache.CacheService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsPerson;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceUpdateTest {

	@InjectMocks
	PersonServiceImpl personService;

	@Mock
	PersonConverter personConverter;

	@Mock
	IsimWsClient isimWsClient;

	@Mock
	IsimWsSession isimWsSession;

	@Mock
	IsimUserContextManager isimUserContextManager;

	@Mock
	CacheService cacheService;

	@Test
	@DisplayName("""
			WHEN updating user WITH suspend description
			THEN (multiple) LF's are replaced by '-' 
			""")
	void validateSuspendedDescriptionWhenUpdatingUser() {
		String suspendOmschrijving = "not empty\n\n\nmulti line";
		String suspendOmschrijvingResult = "not empty - multi line";

		var personObject = new PersonObject();
		personObject.setDn(new Dn("erglobalid=1,ou=0,ou=people"));
		personObject.setSuspend(true);
		personObject.setSuspendOmschrijving(suspendOmschrijving);

		IsimWsPerson personOld = mock(IsimWsPerson.class);

		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(isimWsClient.getPersonByDn(isimWsSession, personObject.getDn())).thenReturn(personOld);

		personService.updatePerson(personObject);

		assertThat(personObject.getSuspendOmschrijving()).isEqualTo(suspendOmschrijvingResult);

		verify(cacheService).deleteFromPersonCaches(personObject.getDn());
	}
}
