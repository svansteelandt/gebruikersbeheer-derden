package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.client.token.TokenClient;
import be.vdab.gebruikersbeheer.derden.client.token.TokenClientException;
import be.vdab.gebruikersbeheer.derden.domain.DerdeCreationToken;
import be.vdab.gebruikersbeheer.derden.domain.TokenRequest;
import be.vdab.gebruikersbeheer.derden.domain.TokenResponse;
import be.vdab.gebruikersbeheer.derden.exception.TokenCreationException;
import be.vdab.gebruikersbeheer.derden.extern.command.CreateDerdeCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

	@Mock
	CreateDerdeCommand command;
	@Mock
	TokenClient tokenClient;
	@InjectMocks
	TokenService tokenService;

	@BeforeEach
	void setUp() {
        when(command.getEmailAddress()).thenReturn("dries.thiern@gmail.com");
		when(command.getGsm()).thenReturn("0485446635");
		when(command.getIkp()).thenReturn("12378000");
		when(command.getInsz()).thenReturn("85081236549");
		when(command.getNaam()).thenReturn("Thieren");
		when(command.getTelefoon()).thenReturn("0324826667");
		when(command.getVoornaam()).thenReturn("Dries");
		when(command.getToegangsrechten()).thenReturn(Collections.singletonList("AR-SOMETHING-SOMETHING"));
	}

	@Test
	@DisplayName("When a token is requested with a valid payload then a token is returned")
	void getTokenForDerdeCreation() {
		when(tokenClient.requestToken(tokenRequest())).thenReturn(tokenResponse());

		DerdeCreationToken result = tokenService.getDerdeCreationToken(command);

		assertThat(result.getToken()).isEqualTo(tokenResponse().getToken());
		assertThat(result.getExpirationTime()).isEqualTo(tokenResponse().getExpirationTime());
	}

	@Test
	@DisplayName("When tokenservice fails to obtain a token an exception is thrown")
	void getTokenForDerdeCreation_throwsException() {
		when(tokenClient.requestToken(tokenRequest())).thenThrow(TokenClientException.class);

		assertThatThrownBy(() -> tokenService.getDerdeCreationToken(command))
				.isInstanceOf(TokenCreationException.class);
	}

	private TokenResponse tokenResponse() {
		return TokenResponse.builder()
				.token("ejYSchoonToken")
				.expirationTime(LocalDateTime.of(2022, 1, 1, 1, 1))
				.build();
	}

	private TokenRequest tokenRequest() {
		return TokenRequest.builder()
				.email("dries.thiern@gmail.com")
				.gsm("0485446635")
				.ikp("12378000")
				.insz("85081236549")
				.naam("Thieren")
				.telefoon("0324826667")
				.voornaam("Dries")
				.toegangsrechten(Collections.singletonList("AR-SOMETHING-SOMETHING"))
				.build();
	}
}