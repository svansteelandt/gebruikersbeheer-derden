package be.vdab.gebruikersbeheer.derden.exception;

import org.springframework.web.client.HttpClientErrorException;

public class TokenExchangeException extends RuntimeException {

	public TokenExchangeException(Exception cause) {
		super(cause);
	}

	public boolean isCausedByBadToken() {
		return throwableOrItsCauseIsHttpClientErrorException(getCause());
	}

	private boolean throwableOrItsCauseIsHttpClientErrorException(Throwable cause) {
		return cause != null && (cause instanceof HttpClientErrorException || throwableOrItsCauseIsHttpClientErrorException(cause.getCause()));
	}
}
