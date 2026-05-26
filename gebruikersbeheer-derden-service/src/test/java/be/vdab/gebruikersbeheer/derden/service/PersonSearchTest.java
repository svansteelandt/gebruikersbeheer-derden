package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.converter.PersonConverter;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonSearch;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.LdapFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonSearchTest {

	private static final String USERNAME = "AVHOYE";
	private static final String FIRST_NAME = "Anton";
	private static final String LAST_NAME = "Van Hoye";
	public static final String FULL_NAME = "%s %s".formatted(FIRST_NAME, LAST_NAME);
	private static final String EMAIL = "anton.vanhoye@vdab.be";
	private static final String OE= "1881";

	@InjectMocks
	PersonServiceImpl personService;

	@Mock
	IsimLdapManager isimLdapManager;

	@Mock
	PersonConverter personConverter;

	@Test
	void searchPerson() {
		when(isimLdapManager.getPersonsByFilter(any(LdapFilter.class), any())).thenReturn(new ArrayList<>());
		when(personConverter.convertList(ArgumentMatchers.anyList(), eq(false))).thenReturn(new ArrayList<>());

		PersonSearch search= new PersonSearch();
		search.setGebruikersnaam(USERNAME);
		search.setNaam(LAST_NAME);
		search.setVoornaam(FIRST_NAME);
		search.setVolledigeNaam(FULL_NAME);
		search.setEmail(EMAIL);
		search.setRijksregisternummer(null);
		search.setOe(OE);

		assertThat(search.hasSearchCriteria()).isTrue();

		var query = FindPersonQuery.builder()
				.gebruikersnaam(USERNAME)
				.naam(LAST_NAME)
				.voornaam(FIRST_NAME)
				.volledigeNaam(FULL_NAME)
				.email(EMAIL)
				.rijksregisternummer(null)
				.oe(OE)
				.build();

		List<PersonObject> gebruikers = personService.findPersons(query);
		assertThat(gebruikers).isNotNull();
	}

	@Test
	void searchPersonWithoutCriteria(){
		PersonSearch search= new PersonSearch();

		assertThat(search.hasSearchCriteria()).isFalse();

		String filter= search.getSearchFilter();
		assertThat(filter).isEqualTo("Geen zoekopdracht meegegeven.");
	}
}
