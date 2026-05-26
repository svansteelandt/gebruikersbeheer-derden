package be.vdab.gebruikersbeheer.derden.components.oeservice;

import be.vdab.gebruikersbeheer.derden.components.oeservice.api.ContactAdresOE;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OEServiceClientTest {

	private static final String URL = "http://oeservice/oeservice";

	@Mock
	ApplicationProperties applicationProperties;

	@Mock
	RestTemplate restTemplate;

	@InjectMocks
	OEServiceClientImpl oeServiceClient;

	@Test
	void getPubliekeOmschrijving(){
		long oeId= 1798L;

		ContactAdresOE contactAdresOE= new ContactAdresOE();
		contactAdresOE.setNaam("OE omschrijving");

		String completeUrl= "%s/oe/{oeId}/contact".formatted(URL);

		when(applicationProperties.getOeServiceUrl()).thenReturn(URL);
		when(restTemplate.getForEntity(completeUrl, ContactAdresOE.class, oeId)).thenReturn(new ResponseEntity(contactAdresOE, HttpStatus.OK));

		String publiekeOmschrijving= oeServiceClient.getPubliekeOmschrijving(oeId);

		assertThat(publiekeOmschrijving).isNotNull().isEqualTo(contactAdresOE.getNaam());
	}
}
