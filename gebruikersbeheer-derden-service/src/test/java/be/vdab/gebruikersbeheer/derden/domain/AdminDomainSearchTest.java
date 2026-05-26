package be.vdab.gebruikersbeheer.derden.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AdminDomainSearchTest {

	private static final String ADMIN_NAME = "Testbedrijf";
	private static final String ADMIN_STREET = "Keizerslaan 31";
	private static final String ADMIN_POSTALCODE = "1000";
	private static final String ADMIN_CITY = "Brussel";
	private static final String ADMIN_IKP = "203000";
	private static final String ADMIN_IKP_END = "0";
	private static final String ADMIN_KBONUMMER= "0123.456.789";
	private static final String ADMIN_OE= "10008045";

	@Test
	void setName(){
		AdminDomainSearch search= new AdminDomainSearch();
		search.setName(ADMIN_NAME);

		assertThat(search.hasSearchCriteria()).isTrue();
		assertThat(search.getSearchFilter()).isNotBlank();
	}

	@Test
	void setStraat(){
		AdminDomainSearch search= new AdminDomainSearch();
		search.setStreet(ADMIN_STREET);

		assertThat(search.hasSearchCriteria()).isTrue();
		assertThat(search.getSearchFilter()).isNotBlank();
	}

	@Test
	void setPostcode(){
		AdminDomainSearch search= new AdminDomainSearch();
		search.setPostalcode(ADMIN_POSTALCODE);

		assertThat(search.hasSearchCriteria()).isTrue();
		assertThat(search.getSearchFilter()).isNotBlank();
	}

	@Test
	void setCity(){
		AdminDomainSearch search= new AdminDomainSearch();
		search.setPostalcode(ADMIN_CITY);

		assertThat(search.hasSearchCriteria()).isTrue();
		assertThat(search.getSearchFilter()).isNotBlank();
	}

	@Test
	void setIkp(){
		AdminDomainSearch search= new AdminDomainSearch();
		search.setIkp(ADMIN_IKP);
		search.setIkpEnd(ADMIN_IKP_END);

		assertThat(search.hasSearchCriteria()).isTrue();
		assertThat(search.getSearchFilter()).isNotBlank();
	}

	@Test
	void setKbo(){
		AdminDomainSearch search= new AdminDomainSearch();
		search.setKboNummer(ADMIN_KBONUMMER);

		assertThat(search.hasSearchCriteria()).isTrue();
		assertThat(search.getSearchFilter()).isNotBlank();
	}

	@Test
	void setOe(){
		AdminDomainSearch search= new AdminDomainSearch();
		search.setOe(ADMIN_OE);

		assertThat(search.hasSearchCriteria()).isTrue();
		assertThat(search.getSearchFilter()).isNotBlank();
	}
}
