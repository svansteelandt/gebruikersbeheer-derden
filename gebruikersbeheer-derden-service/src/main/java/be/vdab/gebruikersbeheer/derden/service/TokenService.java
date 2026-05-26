package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.client.token.TokenClient;
import be.vdab.gebruikersbeheer.derden.client.token.TokenClientException;
import be.vdab.gebruikersbeheer.derden.domain.DerdeCreationClaims;
import be.vdab.gebruikersbeheer.derden.domain.DerdeCreationToken;
import be.vdab.gebruikersbeheer.derden.domain.TokenRequest;
import be.vdab.gebruikersbeheer.derden.domain.TokenResponse;
import be.vdab.gebruikersbeheer.derden.exception.TokenCreationException;
import be.vdab.gebruikersbeheer.derden.exception.TokenExchangeException;
import be.vdab.gebruikersbeheer.derden.extern.command.CreateDerdeCommand;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static be.vdab.gebruikersbeheer.derden.config.CacheNames.CACHE_TOKEN_EXCHANGE;

@Service
public class TokenService {

	private final TokenClient tokenClient;

	public TokenService(TokenClient tokenClient) {
		this.tokenClient = tokenClient;
	}

	public DerdeCreationToken getDerdeCreationToken(CreateDerdeCommand command) {
		try {
			TokenResponse tokenResponse = tokenClient.requestToken(new TokenRequest(command));
			return new DerdeCreationToken(tokenResponse.getToken(), tokenResponse.getExpirationTime());
		} catch (TokenClientException ex) {
			throw new TokenCreationException(ex);
		}
	}

	@Cacheable(CACHE_TOKEN_EXCHANGE)
	public DerdeCreationClaims exchangeTokenForClaims(String derdeCreationToken) {
		try {
			return new DerdeCreationClaims(tokenClient.exchangeToken(derdeCreationToken));
		} catch (TokenClientException ex) {
			throw new TokenExchangeException(ex);
		}
	}
}
