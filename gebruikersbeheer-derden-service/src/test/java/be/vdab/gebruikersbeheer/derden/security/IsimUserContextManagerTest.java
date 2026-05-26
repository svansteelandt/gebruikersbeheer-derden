package be.vdab.gebruikersbeheer.derden.security;

import be.vdab.gebruikersbeheer.derden.monitoring.MonitoringService;
import be.vdab.gebruikersbeheer.derden.util.isim.support.IsimSessionService;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.tokenutil.domain.JWTToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class IsimUserContextManagerTest {

	protected static final JWTToken JWT_TOKEN = new JWTToken();
	@Mock
	IsimSessionService isimSessionService;
	@Mock
	IsimWsSession userSession;

	@Mock
	IsimWsSession adminSession;
	@Mock
	MonitoringService monitoringService;

	@InjectMocks
	IsimUserContextManager isimUserContextManager;

	@AfterEach
	void breakdown() {
		isimUserContextManager.clearSessions();
	}

	@Test
	@DisplayName("WHEN a prviliged session is started " +
			"THEN getSession returns the privileged session")
	void startPrivilegedSession() {
		isimUserContextManager.startPrivilegedSession();
		when(isimSessionService.getAdminSession()).thenReturn(adminSession);

		IsimWsSession result = isimUserContextManager.getSession();

		assertThat(result).isEqualTo(adminSession);
	}

	@Test
	@DisplayName("WHEN no priviliged session was started " +
			"AND a jwt token was set" +
			"THEN getSession returns the session of the authenticated user")
	void getSession() {
		when(isimSessionService.getSession(JWT_TOKEN)).thenReturn(userSession);

		isimUserContextManager.setJwt(JWT_TOKEN);
		IsimWsSession result = isimUserContextManager.getSession();

		assertThat(result).isEqualTo(userSession);
	}

	@Test
	@DisplayName("WHEN no priviliged session was started " +
			"AND no jwt token was set" +
			"THEN getSession returns the session of a technical user")
	void getSession2() {
		when(isimSessionService.getAdminSession()).thenReturn(adminSession);

		IsimWsSession result = isimUserContextManager.getSession();

		assertThat(result).isEqualTo(adminSession);
	}

	@Test
	@DisplayName("""
			GIVEN empty isim user context
			WHEN get session
			THEN return the session of a technical user
			""")
	void givenEmptyIsimUserContext_whenGetSession_thenReturnSessionOfTechnicalUser(CapturedOutput output) {
		// Given
		isimUserContextManager.init();
		when(isimSessionService.getAdminSession()).thenReturn(adminSession);

		// When
		IsimWsSession result = isimUserContextManager.getSession();

		// Then
		assertThat(result).isEqualTo(adminSession);
		assertThat(output).contains("Started isim session for user without JWT token.");
	}

	@Test
	@DisplayName("WHEN setJwtToken is called with null " +
			"THEN the token is removed and no authenticated user session can be created ")
	void setJWTToken() {
		isimUserContextManager.setJwt(JWT_TOKEN);
		isimUserContextManager.setJwt(null);

		boolean result = isimUserContextManager.canCreateAuthenticatedUserSession();

		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("WHEN init is called " +
			"THEN adminsession is removed")
	void init() {
		when(isimSessionService.getAdminSession()).thenReturn(adminSession);
		when(isimSessionService.getSession(JWT_TOKEN)).thenReturn(userSession);
		isimUserContextManager.startPrivilegedSession();

		assertThat(isimUserContextManager.getSession()).isEqualTo(adminSession);

		isimUserContextManager.init();

		isimUserContextManager.setJwt(JWT_TOKEN);
		assertThat(isimUserContextManager.getSession()).isEqualTo(userSession);
	}

	@Test
	@DisplayName("WHEN init is called " +
			"THEN JWT token is removed")
	void init2() {
		isimUserContextManager.setJwt(JWT_TOKEN);

		isimUserContextManager.init();

		assertThat(isimUserContextManager.canCreateAuthenticatedUserSession()).isFalse();
	}

	@Test
	@DisplayName("WHEN a priviliged session is ended " +
			"THEN an authenticated user session is returned")
	void priviligedSession() {
		when(isimSessionService.getAdminSession()).thenReturn(adminSession);
		when(isimSessionService.getSession(JWT_TOKEN)).thenReturn(userSession);
		isimUserContextManager.setJwt(JWT_TOKEN);

		isimUserContextManager.startPrivilegedSession();
		assertThat(isimUserContextManager.getSession()).isEqualTo(adminSession);

		isimUserContextManager.endPrivilegedSession();

		assertThat(isimUserContextManager.getSession()).isEqualTo(userSession);
	}
}