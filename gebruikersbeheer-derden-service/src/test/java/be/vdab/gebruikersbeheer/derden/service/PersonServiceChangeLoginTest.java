package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.exception.IsimRuntimeException;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsPerson;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceChangeLoginTest {

	private static final String GLOBAL_ID = "erglobalid=123";

	private static final Dn PERSON_DN = Dn.of("erglobalid=123,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");

	@Mock
	ApplicationProperties properties;

	@Mock
	IsimWsClient isimWsClient;

	@Mock
	IsimWsSession isimWsSession;

	@Mock
	IsimWsPerson isimWsPersonOld, isimWsPersonNew;

	@Mock
	IsimUserContextManager isimUserContextManager;

	@InjectMocks
	PersonServiceImpl personService;

	@BeforeEach
	void init() {
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"1",
			"5",
			"10"
	})
	void changeLoginMethod(String loginMethod) {
		when(properties.createPersonDn(GLOBAL_ID)).thenReturn(PERSON_DN);
		when(isimWsClient.getPersonByDn(isimWsSession, PERSON_DN)).thenReturn(isimWsPersonOld);
		when(isimWsPersonOld.setAuthenticationLevel(loginMethod)).thenReturn(isimWsPersonNew);

		boolean result = personService.changeLoginMethod(GLOBAL_ID, loginMethod);

		assertThat(result).isTrue();
		verify(isimWsClient).updatePerson(eq(isimWsSession), any(IsimWsPerson.class), any(IsimWsPerson.class));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"1",
			"5",
			"10"
	})
	void changeLoginMethodWithException(String loginMethod) {
		doThrow(new IsimRuntimeException("Runtime")).when(isimWsClient).updatePerson(eq(isimWsSession), any(IsimWsPerson.class), any(IsimWsPerson.class));

		when(properties.createPersonDn(GLOBAL_ID)).thenReturn(PERSON_DN);
		when(isimWsClient.getPersonByDn(isimWsSession, PERSON_DN)).thenReturn(isimWsPersonOld);
		when(isimWsPersonOld.setAuthenticationLevel(loginMethod)).thenReturn(isimWsPersonNew);

		boolean result = personService.changeLoginMethod(GLOBAL_ID, loginMethod);

		assertThat(result).isFalse();
	}
}
