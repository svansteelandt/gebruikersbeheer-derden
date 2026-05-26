package be.vdab.gebruikersbeheer.derden.client.token;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.ClaimsResponse;
import be.vdab.gebruikersbeheer.derden.domain.TokenRequest;
import be.vdab.gebruikersbeheer.derden.domain.TokenResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TokenClient {

	private static final String CREATE_DERDE_TOKEN_URL = "/api/derden/token/create-derde";
	private static final String EXCHANGE_TOKEN_URL = "/api/derden/token/create-derde/exchange";
	private final RestTemplate restTemplate;
	private final ApplicationProperties applicationProperties;

	public TokenClient(RestTemplate restTemplate, ApplicationProperties applicationProperties) {
		this.restTemplate = restTemplate;
		this.applicationProperties = applicationProperties;
	}

	public TokenResponse requestToken(TokenRequest request) {
		HttpEntity<TokenRequest> payload = new HttpEntity<>(request, headers());

		String url = applicationProperties.getAuth4uUrl() + CREATE_DERDE_TOKEN_URL;

		ResponseEntity<TokenResponse> response;
		try {
			response = restTemplate.exchange(url, HttpMethod.POST, payload, TokenResponse.class);
		} catch (Exception e) {
			throw new TokenClientException(e);
		}

		return response.getBody();
	}

	public ClaimsResponse exchangeToken(String token) {
		HttpEntity<TokenExchangeRequest> payload = new HttpEntity<>(new TokenExchangeRequest(token), headers());

		String url = applicationProperties.getAuth4uUrl() + EXCHANGE_TOKEN_URL;

		ResponseEntity<ClaimsResponse> response;
		try {
			response = restTemplate.exchange(url, HttpMethod.POST, payload, ClaimsResponse.class);
		} catch (Exception e) {
			throw new TokenClientException(e);
		}

		return response.getBody();
	}

	private HttpHeaders headers() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("X-vdabcallingapplication", "gebruikersbeheer-derden");
		return headers;
	}

	@Data
	@AllArgsConstructor
	private static final class TokenExchangeRequest {
		String token;
	}
}
