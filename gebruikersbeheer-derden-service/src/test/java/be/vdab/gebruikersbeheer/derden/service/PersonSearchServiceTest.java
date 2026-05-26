package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonSearch;
import be.vdab.gebruikersbeheer.derden.domain.PersonStatus;
import be.vdab.gebruikersbeheer.derden.domain.PersonsZoekResultaat;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.isim.constants.AdminDomainNames;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonSearchServiceTest {
	static final String GEBRUIKER_NAAM = "KJESPERS";
	static final String RIJKSREGISTER_NUMMER = "78080216217";
	static final String VOORNAAM = "Jos";
	static final String NAAM = "Vermeulen";
	static final String VOLLEDIGE_NAAM = "Jos Vermeulen";
	static final String EMAIL = "jos@vermeulen.be";
	static final long OE_NUMMER = 1881;
	public static final int LIMIT = 10;

	@InjectMocks
	PersonSearchServiceImpl personSearchService;

	@Mock
	AdminDomainService adminDomainService;
	@Mock
	PersonService personService;

	@Test
	@DisplayName("""
			GIVEN searchcriteria, but auditordomain is not found
			WHEN zoek is called
			THEN VestigingNietGevondenException is thrown
			""")
	void zoekFaaltOmdatAuditorDomainNietGevondenWordt() {
		var gevondenPersons = new ArrayList<PersonObject>();
		var zoekCommand = createZoekCommand();
		when(adminDomainService.findAdminDomainByName(AdminDomainNames.AUDITORS)).thenReturn(Optional.empty());
		when(personService.findPersons(any())).thenReturn(gevondenPersons);

		assertThatThrownBy(() -> personSearchService.zoek(zoekCommand)).isInstanceOf(VestigingNietGevondenException.class).hasMessageContaining(AdminDomainNames.AUDITORS);
	}

	private static PersonSearch createZoekCommand() {
		var zoekCommand = new PersonSearch();
		zoekCommand.setGebruikersnaam(GEBRUIKER_NAAM);
		zoekCommand.setRijksregisternummer(RIJKSREGISTER_NUMMER);
		zoekCommand.setVoornaam(VOORNAAM);
		zoekCommand.setNaam(NAAM);
		zoekCommand.setVolledigeNaam(VOLLEDIGE_NAAM);
		zoekCommand.setEmail(EMAIL);
		zoekCommand.setOe(Long.toString(OE_NUMMER));
		zoekCommand.setLimit(LIMIT);
		return zoekCommand;
	}

	private static FindPersonQuery createFindPersonQuery() {
		return FindPersonQuery.builder()
				.gebruikersnaam(GEBRUIKER_NAAM)
				.rijksregisternummer(RIJKSREGISTER_NUMMER)
				.voornaam(VOORNAAM)
				.naam(NAAM)
				.volledigeNaam(VOLLEDIGE_NAAM)
				.email(EMAIL)
				.oe(Long.toString(OE_NUMMER))
				.limit(LIMIT)
				.build();
	}

	@Test
	@DisplayName("""
			GIVEN searchcriteria, auditordomain found
			WHEN zoek is called
			THEN results are returned where ikps contains the list of passive persons of which the ikp exists
			""")
	void zoeken() {
		var auditorDomain = new AdminDomainObject();
		var actievePerson = createPerson(PersonStatus.ACTIVE, 203000L);
		var passievePerson = createPerson(PersonStatus.INACTIVE, 203001L);
		var passievePersonWaarvanIkpNietBestaat = createPerson(PersonStatus.INACTIVE, 203002L);
		var gevondenPersons = new ArrayList<PersonObject>();
		gevondenPersons.add(actievePerson);
		gevondenPersons.add(passievePerson);
		gevondenPersons.add(passievePersonWaarvanIkpNietBestaat);
		var gevondenIkps = List.of(passievePerson.getIkp());
		var zoekCommand = createZoekCommand();
		when(adminDomainService.findAdminDomainByName(AdminDomainNames.AUDITORS)).thenReturn(Optional.of(auditorDomain));
		when(personService.findPersons(createFindPersonQuery())).thenReturn(gevondenPersons);
		when(adminDomainService.findAdminDomainByName(AdminDomainNames.AUDITORS)).thenReturn(Optional.of(auditorDomain));
		when(adminDomainService.existsAdminDomain(passievePerson.getIkp())).thenReturn(true);
		when(adminDomainService.existsAdminDomain(passievePersonWaarvanIkpNietBestaat.getIkp())).thenReturn(false);

		var result = personSearchService.zoek(zoekCommand);

		var teVindenZoekResultaat = new PersonsZoekResultaat(gevondenPersons, gevondenIkps);
		assertThat(result).usingRecursiveComparison().isEqualTo(teVindenZoekResultaat);
	}

	private static PersonObject createPerson(PersonStatus status, long ikp) {
		var passievePerson = new PersonObject();
		passievePerson.setStatus(status);
		passievePerson.setIkp(Ikp.of(ikp));
		return passievePerson;
	}
}