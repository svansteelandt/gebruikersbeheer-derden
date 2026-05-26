package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper.GebruikerMapper;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerSummaryDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.ChangeLoginMethodCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.CreateGebruikerCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.EditGebruikerCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.WijsRollenToeAanGebruikerCommand;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.LoginMethod;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonSearch;
import be.vdab.gebruikersbeheer.derden.domain.PersonsZoekResultaat;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.GebruikerHeeftGeenEmailAdresException;
import be.vdab.gebruikersbeheer.derden.exception.LoginMethodeCouldNotBeChangedException;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.SearchCriteriaVerplichtException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import be.vdab.gebruikersbeheer.derden.service.AccountService;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.PersonChangeRolesService;
import be.vdab.gebruikersbeheer.derden.service.PersonCreateService;
import be.vdab.gebruikersbeheer.derden.service.PersonRestoreService;
import be.vdab.gebruikersbeheer.derden.service.PersonSearchService;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GebruikersRestControllerTest {
	public static final String ROLE_ID = "1234";
	public static final String VESTIGING_ID = "123";
	public static final String GEBRUIKER_ID = "1";
	public static final String GEBRUIKER_NAAM = "KJESPERS";
	public static final String RIJKSREGISTER_NUMMER = "78080216217";
	public static final String VOORNAAM = "Jos";
	public static final String NAAM = "Vermeulen";
	public static final String VOLLEDIGE_NAAM = "Jos Vermeulen";
	public static final String EMAIL = "jos@vermeulen.be";
	public static final long OE_NUMMER = 1881;
	@Mock
	PersonService personService;
	@Mock
	GebruikerMapper gebruikerMapper;
	@Mock
	AdminDomainService adminDomainService;
	@Mock
	ApplicationProperties applicationProperties;
	@Mock
	PersonCreateService personCreateService;
	@Mock
	PersonSearchService personSearchService;
	@Mock
	PersonRestoreService personRestoreService;
	@Mock
	PersonChangeRolesService personChangeRolesService;
	@Mock
	AccountService accountService;
	@Mock
	RoleService roleService;
	@InjectMocks
	GebruikerRestController gebruikerRestController;

	@Test
	@DisplayName("""
			GIVEN no searchcriteria
			WHEN zoek is called
			THEN SearchCriteriaVerplichtException is thrown
			""")
	void zoekZonderCriteria() {
		assertThatThrownBy(() -> gebruikerRestController.zoek(null, null, null, null, null, null, null)).isInstanceOf(SearchCriteriaVerplichtException.class);
	}


	@Test
	@DisplayName("""
			GIVEN searchcriteria
			WHEN zoek is called
			THEN service is called and persons mapped to gebruikers
			""")
	void zoekRoeptServiceOpMetJuisteCommand() throws Exception {
		var gevondenPersons = new ArrayList<PersonObject>();
		var gevondenGebruikers = new ArrayList<GebruikerSummaryDto>();
		var gevondenIkps = new ArrayList<Ikp>();

		var teVindenZoekResultaat = new PersonsZoekResultaat(gevondenPersons, gevondenIkps);
		when(personSearchService.zoek(any(PersonSearch.class))).thenReturn(teVindenZoekResultaat);
		when(gebruikerMapper.map(gevondenPersons)).thenReturn(gevondenGebruikers);

		var result = gebruikerRestController.zoek(GEBRUIKER_NAAM, RIJKSREGISTER_NUMMER, VOORNAAM, NAAM, VOLLEDIGE_NAAM, EMAIL, OE_NUMMER);
		assertThat(result.gevondenIkps()).isEqualTo(gevondenIkps.stream().map(Ikp::getDisplayValue).toList());
		assertThat(result.gebruikers()).isEqualTo(gevondenGebruikers);

		var personSearchArgumentCaptor = ArgumentCaptor.forClass(PersonSearch.class);
		verify(personSearchService).zoek(personSearchArgumentCaptor.capture());
		var gevondenCommand = personSearchArgumentCaptor.getValue();
		assertThat(gevondenCommand.getOe()).isEqualTo(Long.toString(OE_NUMMER));
		assertThat(gevondenCommand.getEmail()).isEqualTo(EMAIL);
		assertThat(gevondenCommand.getNaam()).isEqualTo(NAAM);
		assertThat(gevondenCommand.getVoornaam()).isEqualTo(VOORNAAM);
		assertThat(gevondenCommand.getVolledigeNaam()).isEqualTo(VOLLEDIGE_NAAM);
		assertThat(gevondenCommand.getGebruikersnaam()).isEqualTo(GEBRUIKER_NAAM);
		assertThat(gevondenCommand.getRijksregisternummer()).isEqualTo(RIJKSREGISTER_NUMMER);
	}

	@Test
	@DisplayName("""
			GIVEN valid person, valid loginmethod and logged in user has ROL_CVS_RFI
			WHEN changeLoginMethod is called
			THEN person service changeLoginMethod is called
			""")
	void changeLoginMethodCallsPersonService() throws Exception {
		when(personService.changeLoginMethod(GEBRUIKER_ID, LoginMethod.ACM.getValue())).thenReturn(true);
		gebruikerRestController.changeLoginMethod(GEBRUIKER_ID, new ChangeLoginMethodCommand(LoginMethod.ACM));
		verify(personService).changeLoginMethod(GEBRUIKER_ID, LoginMethod.ACM.getValue());
	}

	@Test
	@DisplayName("""
			GIVEN valid person, valid loginmethod and logged in user has ROL_CVS_RFI
			WHEN changeLoginMethod is called but personservice returns false
			THEN LoginMethodeCouldNotBeChangedException is thrown
			""")
	void changeLoginMethodThrowsExceptionWhenPersonServiceHasntChangedTheLoginMethod() {
		when(personService.changeLoginMethod(GEBRUIKER_ID, LoginMethod.ACM.getValue())).thenReturn(false);
		assertThatThrownBy(() -> gebruikerRestController.changeLoginMethod(GEBRUIKER_ID, new ChangeLoginMethodCommand(LoginMethod.ACM))).isInstanceOf(LoginMethodeCouldNotBeChangedException.class);
		verify(personService).changeLoginMethod(GEBRUIKER_ID, LoginMethod.ACM.getValue());
	}

	@Test
	@DisplayName("""
			GIVEN non existing vestiging and user has role to delete a user
			WHEN deleteGebruiker is called
			THEN VestigingNietGevondenException is thrown
			""")
	void verwijderenVanGebruikerVanNietBestaandeOrganisatie() {
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.empty());
		assertThatThrownBy(() -> gebruikerRestController.deleteGebruiker(VESTIGING_ID, GEBRUIKER_ID)).isInstanceOf(VestigingNietGevondenException.class);
	}

	@Test
	@DisplayName("""
			GIVEN existing vestiging id
			WHEN deleteGebruiker is called
			THEN person service deleteGebruiker is called
			""")
	void gebruikersVanBestaandeVestigingOpvragen() {
		var vestiging = new AdminDomainObject();
		vestiging.setDn(Dn.of("erglobalid=" + VESTIGING_ID));
		var person = new PersonObject();
		var persons = List.of(person);
		when(applicationProperties.createAdminDomainDn(VESTIGING_ID)).thenReturn(vestiging.getDn());
		when(adminDomainService.findAdminDomainByDnWithRoles(vestiging.getDn())).thenReturn(Optional.of(vestiging));
		when(personService.findNonMlpOpleidingPersonsFromOrganization(vestiging)).thenReturn(persons);
		var teVindenGebruikers = new ArrayList<GebruikerSummaryDto>();
		when(gebruikerMapper.map(persons)).thenReturn(teVindenGebruikers);
		var gevondenGebruikers = gebruikerRestController.getGebruikersVanVestiging(VESTIGING_ID);
		assertThat(gevondenGebruikers).isEqualTo(teVindenGebruikers);
	}

	@Test
	@DisplayName("""
			GIVEN non existing vestiging id
			WHEN deleteGebruiker is called
			THEN VestigingNietGevondenException will be thrown
			""")
	void gebruikersVanNietBestaandeBestaandeVestigingOpvragen() {
		var vestiging = new AdminDomainObject();
		vestiging.setDn(Dn.of("erglobalid=" + VESTIGING_ID));
		when(applicationProperties.createAdminDomainDn(VESTIGING_ID)).thenReturn(vestiging.getDn());
		when(adminDomainService.findAdminDomainByDnWithRoles(vestiging.getDn())).thenReturn(Optional.empty());
		assertThatThrownBy(() -> gebruikerRestController.getGebruikersVanVestiging(VESTIGING_ID)).isInstanceOf(VestigingNietGevondenException.class);
	}

	@Test
	@DisplayName("""
			GIVEN valid vestigingId and command
			WHEN createGebruiker is called
			THEN personCreateService is called
			""")
	void createGebruiker() throws Exception {
		var command = new CreateGebruikerCommand("Jos", "vermeulen", "78080216217", "test@vdab.be", "03657777", "0486337477", List.of(ROLE_ID), false);
		var role = new RoleObject();
		var person = new PersonObject();
		when(roleService.findRoleByGlobalId(ROLE_ID)).thenReturn(Optional.of(role));
		when(gebruikerMapper.mapToPerson(command)).thenReturn(person);
		gebruikerRestController.createGebruiker(VESTIGING_ID, command);
		verify(personCreateService).create(VESTIGING_ID, person, List.of(role));
		assertThat(role.isChanged()).isTrue();
		assertThat(role.getHasRole()).isTrue();
	}

	@Test
	@DisplayName("""
			GIVEN valid gebruikerId
			WHEN restore is called
			THEN personRestoreService is called
			""")
	void restoreGebruiker() {
		gebruikerRestController.restore(GEBRUIKER_ID);
		verify(personRestoreService).restore(GEBRUIKER_ID);
	}

	@Test
	@DisplayName("""
			GIVEN gebruikerId of existing user with email
			WHEN verstuurResetWachtwoordMail is called
			THEN accountservice is called to reset password
			""")
	void verstuurResetWachtwoordMailPassword() throws Exception {
		var gebruikerDn = Dn.of("erglobalid=" + GEBRUIKER_ID + ",");
		var personObject = new PersonObject();
		personObject.setEmailAddress("a@a.com");
		personObject.setUserId("aa");
		when(applicationProperties.createPersonDn(GEBRUIKER_ID)).thenReturn(gebruikerDn);
		when(personService.findPersonByDn(gebruikerDn, null)).thenReturn(Optional.of(personObject));
		gebruikerRestController.verstuurResetWachtwoordMail(GEBRUIKER_ID);
		verify(accountService).resetPassword(personObject.getUserId(), personObject.getEmailAddress());
	}

	@Test
	@DisplayName("""
			GIVEN gebruikerId of existing user without email
			WHEN verstuurResetWachtwoordMail is called
			THEN GebruikerHeeftGeenEmailAdresException is thrown
			""")
	void verstuurResetWachtwoordMailMailOfPersonWithoutEmail() {
		var gebruikerDn = Dn.of("erglobalid=" + GEBRUIKER_ID + ",");
		var personObject = new PersonObject();
		personObject.setEmailAddress(null);
		personObject.setUserId("aa");
		when(applicationProperties.createPersonDn(GEBRUIKER_ID)).thenReturn(gebruikerDn);
		when(personService.findPersonByDn(gebruikerDn, null)).thenReturn(Optional.of(personObject));
		assertThatThrownBy(() -> gebruikerRestController.verstuurResetWachtwoordMail(GEBRUIKER_ID)).isInstanceOf(GebruikerHeeftGeenEmailAdresException.class);
		verify(accountService, never()).resetPassword(personObject.getUserId(), personObject.getEmailAddress());
	}

	@Test
	@DisplayName("""
			GIVEN gebruikerId of non existing user
			WHEN verstuurResetWachtwoordMail is called
			THEN PersonNotFoundException is thrown
			""")
	void verstuurResetWachtwoordMailOfNonExistingUser() {
		var gebruikerDn = Dn.of("erglobalid=" + GEBRUIKER_ID + ",");
		var personObject = new PersonObject();
		personObject.setEmailAddress("a@a.com");
		personObject.setUserId("aa");
		when(applicationProperties.createPersonDn(GEBRUIKER_ID)).thenReturn(gebruikerDn);
		when(personService.findPersonByDn(gebruikerDn, null)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> gebruikerRestController.verstuurResetWachtwoordMail(GEBRUIKER_ID)).isInstanceOf(PersonNotFoundException.class);
	}

	@Test
	@DisplayName("""
			GIVEN gebruikerId of existing user with valid assign roles command
			WHEN wijzigRollenGebruiker is called
			THEN personChangeRolesService is called to assign roles
			""")
	void wijzigRollenGebruiker() {
		var ROLE_IDS = List.of("1");
		var command = new WijsRollenToeAanGebruikerCommand(ROLE_IDS, "cvs rol");

		gebruikerRestController.wijzigRollenGebruiker(GEBRUIKER_ID, command);
		verify(personChangeRolesService).changeRoles(GEBRUIKER_ID, ROLE_IDS, "cvs rol");
	}

	@Test
	@DisplayName("""
			GIVEN gebruikerGlobalId and editGebruiker command with suspend: false
			WHEN editGebruiker is called
			THEN personService is called with empty suspend reason
			""")
	void editGebruikerNotSuspended() {
		var command = new EditGebruikerCommand("First", "Last", "first.last@email.com", "093452656", "0451632445", false, "not empty");
		var personObject = new PersonObject();
		personObject.setSuspend(true);
		personObject.setSuspendOmschrijving("Test omschrijving");

		when(personService.findPersonWithRolesByGlobalId(any())).thenReturn(Optional.of(personObject));
		gebruikerRestController.editGebruiker("globalId", command);

		assertThat(personObject.getFirstName()).isEqualTo("First");
		assertThat(personObject.getLastName()).isEqualTo("Last");
		assertThat(personObject.getEmailAddress()).isEqualTo("first.last@email.com");
		assertThat(personObject.getPhone()).isEqualTo("093452656");
		assertThat(personObject.getMobile()).isEqualTo("0451632445");
		assertThat(personObject.isSuspend()).isFalse();
		assertThat(personObject.getSuspendOmschrijving()).isNull();

		verify(personService).updatePerson(personObject);
	}

	@Test
	@DisplayName("""
			GIVEN gebruikerGlobalId and editGebruiker command with suspend: true
			WHEN editGebruiker is called
			THEN personService is called with valid suspend reason
			""")
	void editGebruikerIsSuspended() {
		var command = new EditGebruikerCommand("First", "Last", "first.last@email.com", "093452656", "0451632445", true, "not empty");
		var personObject = new PersonObject();
		personObject.setSuspend(false);
		personObject.setSuspendOmschrijving("");

		when(personService.findPersonWithRolesByGlobalId(any())).thenReturn(Optional.of(personObject));
		gebruikerRestController.editGebruiker("globalId", command);

		assertThat(personObject.isSuspend()).isTrue();
		assertThat(personObject.getSuspendOmschrijving()).isEqualTo("not empty");
	}
}
