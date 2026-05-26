package be.vdab.gebruikersbeheer.derden.client;

import be.vdab.gebruikersbeheer.derden.client.token.TokenClient;
import be.vdab.gebruikersbeheer.derden.client.token.TokenClientException;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.TokenRequest;
import be.vdab.gebruikersbeheer.derden.domain.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(MockitoExtension.class)
class CreateDerdeTokenClientTest {

	static final TokenRequest TOKEN_REQUEST = tokenRequest();
	static final String TOKEN = "eyJschoonToken";
	static final String AUTH4U_BASE_URL = "http://www.vdab.be/auth4u";
	static final String CREATE_DERDE_TOKEN_URL = AUTH4U_BASE_URL + "/api/derden/token/create-derde";
	static final LocalDateTime TOKEN_EXPIRATION_TIME = LocalDateTime.now().plusDays(14);
	static final TokenResponse TOKEN_RESPONSE = tokenResponse();

	@Mock
	ApplicationProperties applicationProperties;
	@Mock
	RestTemplate restTemplate;
	@InjectMocks
	TokenClient tokenClient;

	@BeforeEach
	void setUp() {
		when(applicationProperties.getAuth4uUrl()).thenReturn(AUTH4U_BASE_URL);
	}

	@Test
	@DisplayName("when a valid payload is provided " +
			"and auth4u is operational " +
			"then a token is returned")
	void requestToken() {
		ResponseEntity<TokenResponse> response = new ResponseEntity<>(TOKEN_RESPONSE, HttpStatus.OK);
		when(restTemplate.exchange(eq(CREATE_DERDE_TOKEN_URL), eq(POST), any(HttpEntity.class), eq(TokenResponse.class))).thenReturn(response);

		TokenResponse result = tokenClient.requestToken(TOKEN_REQUEST);

		assertThat(result.getToken()).isEqualTo(TOKEN);
		assertThat(result.getExpirationTime()).isEqualTo(TOKEN_EXPIRATION_TIME);
	}

	@Test
	@DisplayName("when a bad request is sent to auth4u " +
			"then a correct exception is thrown")
	void requestToken2() {
		whenPostingToCreateDerdeTokenUrl()
				.thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

		assertThatThrownBy(() -> tokenClient.requestToken(TOKEN_REQUEST))
				.isInstanceOf(TokenClientException.class)
				.hasCauseInstanceOf(HttpClientErrorException.class);
	}

	@Test
	@DisplayName("when auth4u returns 5XX response " +
			"then a correct exception is thrown")
	void requestToken3() {
		whenPostingToCreateDerdeTokenUrl()
				.thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

		assertThatThrownBy(() -> tokenClient.requestToken(TOKEN_REQUEST))
				.isInstanceOf(TokenClientException.class)
				.hasCauseInstanceOf(HttpServerErrorException.class);
	}

	@Test
	@DisplayName("when auth4u times-out " +
			"then a correct exception is thrown")
	void requestToken4() {
		whenPostingToCreateDerdeTokenUrl()
				.thenThrow(new ResourceAccessException("Timed-out!"));

		assertThatThrownBy(() -> tokenClient.requestToken(TOKEN_REQUEST))
				.isInstanceOf(TokenClientException.class)
				.hasCauseInstanceOf(ResourceAccessException.class);
	}

	private OngoingStubbing<ResponseEntity<TokenResponse>> whenPostingToCreateDerdeTokenUrl() {
		return when(restTemplate.exchange(eq(CREATE_DERDE_TOKEN_URL), eq(POST), any(HttpEntity.class), eq(TokenResponse.class)));
	}

	private static TokenRequest tokenRequest() {
		return TokenRequest.builder()
				.email("jeoren@asfpj.be")
				.gsm("0478731201")
				.ikp("1234000")
				.insz("90122718707")
				.naam("Meys")
				.voornaam("Joske")
				.telefoon("016236836")
				.toegangsrechten(Collections.singletonList("BOEIT-NI"))
				.build();
	}

	private static TokenResponse tokenResponse() {
		return TokenResponse.builder()
				.token(TOKEN)
				.expirationTime(TOKEN_EXPIRATION_TIME)
				.build();
	}
}