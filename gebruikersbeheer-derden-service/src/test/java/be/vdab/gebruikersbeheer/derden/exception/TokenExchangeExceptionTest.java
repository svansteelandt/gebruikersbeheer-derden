package be.vdab.gebruikersbeheer.derden.exception;

import be.vdab.gebruikersbeheer.derden.client.token.TokenClientException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;

class TokenExchangeExceptionTest {

	@Test
	void isCausedByBadToken() {
		assertThat(new TokenExchangeException(new TokenClientException(new RuntimeException("Boem!"))).isCausedByBadToken()).isFalse();
		assertThat(new TokenExchangeException(null).isCausedByBadToken()).isFalse();

		assertThat(new TokenExchangeException(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).isCausedByBadToken()).isTrue();
		assertThat(new TokenExchangeException(new TokenClientException(new HttpClientErrorException(HttpStatus.BAD_REQUEST))).isCausedByBadToken()).isTrue();
	}
}