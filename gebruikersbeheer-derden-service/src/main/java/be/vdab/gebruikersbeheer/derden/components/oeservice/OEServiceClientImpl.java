package be.vdab.gebruikersbeheer.derden.components.oeservice;

import be.vdab.gebruikersbeheer.derden.components.oeservice.api.ContactAdresOE;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.config.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class OEServiceClientImpl implements OEServiceClient {
	private final ApplicationProperties applicationProperties;
	private final RestTemplate restTemplate;

	public OEServiceClientImpl(ApplicationProperties applicationProperties, RestTemplate restTemplate) {
		this.applicationProperties = applicationProperties;
		this.restTemplate = restTemplate;
	}

	@Cacheable(value = CacheNames.CACHE_OE_NAMEN, unless = "#result == null")
	@Override
	public String getPubliekeOmschrijving(long oeId) {
		String url = "%s/oe/{oeId}/contact".formatted(this.applicationProperties.getOeServiceUrl());

		try {
			ResponseEntity<ContactAdresOE> response = restTemplate.getForEntity(url, ContactAdresOE.class, oeId);

			if (response.getStatusCode().is2xxSuccessful()) {
				ContactAdresOE contactAdresOE = response.getBody();

				if (contactAdresOE != null) {
					return contactAdresOE.getNaam();
				}
			}
		} catch (RestClientException e) {
			// geen omschrijving teruggeven
		}

		return null;
	}
}
