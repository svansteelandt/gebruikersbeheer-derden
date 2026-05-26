package be.vdab.gebruikersbeheer.derden.intern.validator;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainSearch;
import be.vdab.gebruikersbeheer.derden.domain.PersonSearch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;

@ExtendWith(MockitoExtension.class)
class SearchFormValidatorTest {

	private static final String STRING_WITH_LENGTH_169= "Dit is een test. Dit is een test. Dit is een test. Dit is een test. Dit is een test. Dit is een test. Dit is een test. Dit is een test. Dit is een test. Dit is een test.";

	@Mock
	Errors errors;

	@Test
	void searchAdminDomainWithCriteria(){
		AdminDomainSearch adminDomainSearch= new AdminDomainSearch();
		adminDomainSearch.setIkp("203");
		adminDomainSearch.setIkpEnd("0");
		adminDomainSearch.setCity("Brussel");
		adminDomainSearch.setStreet("Keizerslaan 123");
		adminDomainSearch.setPostalcode("1020");
		adminDomainSearch.setOe("10008045");

		SearchFormValidator searchFormValidator= new SearchFormValidator();
		searchFormValidator.validate(adminDomainSearch, errors);
	}

	@Test
	void searchAdminDomainMaxLengtes(){
		AdminDomainSearch adminDomainSearch= new AdminDomainSearch();
		adminDomainSearch.setName(STRING_WITH_LENGTH_169);
		adminDomainSearch.setIkp(STRING_WITH_LENGTH_169);
		adminDomainSearch.setIkpEnd(STRING_WITH_LENGTH_169);
		adminDomainSearch.setCity(STRING_WITH_LENGTH_169);
		adminDomainSearch.setStreet(STRING_WITH_LENGTH_169);
		adminDomainSearch.setPostalcode(STRING_WITH_LENGTH_169);
		adminDomainSearch.setOe(STRING_WITH_LENGTH_169);

		SearchFormValidator searchFormValidator= new SearchFormValidator();
		searchFormValidator.validate(adminDomainSearch, errors);
	}

	@Test
	void searchAdminDomainWithoutCriteria(){
		AdminDomainSearch adminDomainSearch= new AdminDomainSearch();
		adminDomainSearch.setCity("");
		adminDomainSearch.setStreet("");
		adminDomainSearch.setPostalcode("");
		adminDomainSearch.setIkp("");
		adminDomainSearch.setIkpEnd("");
		adminDomainSearch.setOe("");

		SearchFormValidator searchFormValidator= new SearchFormValidator();
		searchFormValidator.validate(adminDomainSearch, errors);
	}

	@Test
	void searchPersonWithCriteria(){
		PersonSearch personSearch= new PersonSearch();
		personSearch.setOe("10008045");

		SearchFormValidator searchFormValidator= new SearchFormValidator();
		searchFormValidator.validate(personSearch, errors);
	}

	@Test
	void  searchPersonWithoutCriteria(){
		PersonSearch personSearch= new PersonSearch();

		SearchFormValidator searchFormValidator= new SearchFormValidator();
		searchFormValidator.validate(personSearch, errors);
	}
}
