package be.vdab.gebruikersbeheer.derden.util.isim.support;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.exception.ITIMContextManagerException;
import be.vdab.gebruikersbeheer.derden.monitoring.MonitoringService;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWebservices;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.client.ResettableWSSessionService;
import be.vdab.tokenutil.domain.JWTToken;
import com.ibm.itim.ws.services.session.WSInvalidLoginException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IsimSessionServiceTest {

	@Mock
	private IsimWebservices isimWebservices;
	@Mock
	IsimWsSession isimWsSession;
	@Mock
	ResettableWSSessionService resettableWSSessionService;
	@Mock
	ApplicationProperties properties;
	@Mock
	MonitoringService monitoringService;
	@InjectMocks
	private IsimSessionService isimSessionService;

	@Test
	@DisplayName("WHEN getSession is called " +
			"THEN an ltpa token is retrieved from ISIM " +
			"AND a session is retrieved with that token")
	void getSession() {
		when(isimWebservices.getLtpaFromJWT(any())).thenReturn("ltpa");
		when(isimWebservices.getISIMSessionByLTPAToken("ltpa")).thenReturn(isimWsSession);

		IsimWsSession result  = isimSessionService.getSession(new JWTToken());

		assertThat(result).isEqualTo(isimWsSession);
	}

	@Test
	@DisplayName("WHEN getSession is called " +
			"AND isimwebservices throws an exception " +
			"THEN an ITIMContextManagerException is thrown")
	void getSession2() {
		when(isimWebservices.getLtpaFromJWT(any())).thenReturn("ltpa");
		when(isimWebservices.getISIMSessionByLTPAToken("ltpa")).thenThrow(new RuntimeException());
		JWTToken jwt = new JWTToken();

		assertThatThrownBy(() -> isimSessionService.getSession(jwt)).isInstanceOf(ITIMContextManagerException.class);
	}

	@Test
	@DisplayName("WHEN getAdminSession is called " +
			"AND resettableWSSessionService throws an exception " +
			"THEN an ITIMContextManagerException is thrown")
	void getAdminSession() throws Exception {
		when(resettableWSSessionService.login(any(), any())).thenThrow(new WSInvalidLoginException("blub"));

		assertThatThrownBy(() -> isimSessionService.getAdminSession()).isInstanceOf(ITIMContextManagerException.class);
	}
}