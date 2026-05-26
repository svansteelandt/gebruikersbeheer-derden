package be.vdab.gebruikersbeheer.derden.adapter.out.rest.config;

import be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.ApiClient;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CodesClientApiConfiguration {

	@Bean
	@Primary
	public ApiClient codesApiClient(RestTemplate restTemplate, ApplicationProperties applicationProperties) {
		ApiClient apiClient = new ApiClient(restTemplate);
		apiClient.setBasePath(applicationProperties.getCodesUrl());
		return apiClient;
	}
}