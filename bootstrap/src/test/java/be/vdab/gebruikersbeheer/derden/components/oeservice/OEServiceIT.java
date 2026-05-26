package be.vdab.gebruikersbeheer.derden.components.oeservice;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OEServiceIT extends BaseIT {

	private OEServiceClient oeServiceClient;

	@BeforeEach
	void createOEServiceClient() {
		oeServiceClient = new OEServiceClientImpl(applicationProperties, restTemplate);
	}

	@Test
	void getPubliekeOmschrijving(){
		long oeId= 1798L;

		String publiekeOmschrijving= oeServiceClient.getPubliekeOmschrijving(oeId);

		assertThat(publiekeOmschrijving).isNotNull();
	}
}
