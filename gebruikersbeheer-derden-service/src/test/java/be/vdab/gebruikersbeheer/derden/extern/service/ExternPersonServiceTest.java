package be.vdab.gebruikersbeheer.derden.extern.service;

import be.vdab.gebruikersbeheer.derden.client.token.TokenClientException;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.DerdeCreationClaims;
import be.vdab.gebruikersbeheer.derden.domain.IsimUserData;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.TokenExchangeException;
import be.vdab.gebruikersbeheer.derden.extern.command.CreateDerdeCommand;
import be.vdab.gebruikersbeheer.derden.monitoring.MonitoringService;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.security.SecurityExpressions;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.derden.service.TokenService;
import be.vdab.gebruikersbeheer.derden.util.isim.support.ContextManager;
import be.vdab.gebruikersbeheer.derden.util.isim.support.IsimSessionService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.exception.IsimApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternPersonServiceTest {

	static final String FULL_NAME = "Dries Thieren";
	static final String INSZ = "nietbelangrijk";
	static final String VALID_TOKEN = "eySchoonTokenSeg";
	static final Ikp IKP = Ikp.of("1234000");
	static final String ORGANISATION_NAME = "Joske BV";
	static final String MOBILE_NUMBER = "0478731201";
	static final String EMAIL_ADDRESS = "jeroen@asfpj.be";
	public static final String TOKEN_INSZ = "90122718707";

	@Mock
	PersonService personService;
	@Mock
	TokenService tokenService;
	@Mock
	RoleService roleService;
	@Mock
	SecurityExpressions securityExpressions;
	@Mock
	AdminDomainService adminDomainService;
	@Mock
	IsimUserData isimUserData;

	@Mock
	IsimSessionService isimSessionService;

	@Mock
	MonitoringService monitoringService;

	@Mock
	ContextManager contextManager;

	private IsimUserContextManager isimUserContextManager;

	@InjectMocks
	ExternPersonService externPersonService;

	@BeforeEach
	void setUp() {
		isimUserContextManager = new IsimUserContextManager(isimSessionService, monitoringService);
	}

	@Test
	void createPerson_insertPersonFailed_failure() {
		when(personService.findPersons(any())).thenReturn(Collections.emptyList());
		when(personService.insertPerson(any(), any(), anyBoolean())).thenThrow(IsimApplicationException.class);

		CreateDerdeCommand createDerdeCommand = mock(CreateDerdeCommand.class);
		when(createDerdeCommand.getInsz()).thenReturn(INSZ);

		ExternPersonService.CreatePersonResult result = externPersonService.createDerdeOrSendMail(createDerdeCommand);

		assertThat(result).isEqualTo(ExternPersonService.CreatePersonResult.failure("errorCreate", "Fout bij het aanmaken van een persoon."));
	}

	@Test
	void createPerson_findPersonByDnNotFound_personNotFoundException() {
		when(personService.findPersons(any())).thenReturn(Collections.emptyList());
		when(personService.findPersonByDn(any(), any())).thenReturn(Optional.empty());

		CreateDerdeCommand createDerdeCommand = mock(CreateDerdeCommand.class);
		when(createDerdeCommand.getInsz()).thenReturn(INSZ);

		ExternPersonService.CreatePersonResult result = externPersonService.createDerdeOrSendMail(createDerdeCommand);

		assertThat(result).isEqualTo(ExternPersonService.CreatePersonResult.failure("errorCreate", "Fout bij het aanmaken van een persoon."));
	}

	@Test
	void createPerson_insertWithoutRoles_success() {
		when(personService.findPersons(any())).thenReturn(Collections.emptyList());
		when(personService.findPersonByDn(any(), any())).thenReturn(Optional.of(new PersonObject()));

		CreateDerdeCommand createDerdeCommand = mock(CreateDerdeCommand.class);
		when(createDerdeCommand.getInsz()).thenReturn(INSZ);
		when(createDerdeCommand.getPersonToInsert()).thenReturn(new PersonObject());
		when(createDerdeCommand.getFullName()).thenReturn(FULL_NAME);

		ExternPersonService.CreatePersonResult result = externPersonService.createDerdeOrSendMail(createDerdeCommand);

		assertThat(result).isEqualTo(ExternPersonService.CreatePersonResult.success("insertPersonWithoutRoles", FULL_NAME));
	}

	@Test
	void createPerson_insertWithRoles_success() {
		when(personService.findPersons(any())).thenReturn(Collections.emptyList());
		when(personService.findPersonByDn(any(), any())).thenReturn(Optional.of(new PersonObject()));

		CreateDerdeCommand createDerdeCommand = mock(CreateDerdeCommand.class);
		when(createDerdeCommand.getInsz()).thenReturn(INSZ);
		PersonObject personToInsert = new PersonObject();
		personToInsert.setVdabUid("");
		personToInsert.setUserId("");
		personToInsert.setRoles(Collections.singletonList(new RoleObject()));
		when(createDerdeCommand.getPersonToInsert()).thenReturn(personToInsert);
		when(createDerdeCommand.getFullName()).thenReturn(FULL_NAME);
		when(createDerdeCommand.getRolesToAdd()).thenReturn(Collections.singletonList(new RoleObject()));

		ExternPersonService.CreatePersonResult result = externPersonService.createDerdeOrSendMail(createDerdeCommand);

		assertThat(result).isEqualTo(ExternPersonService.CreatePersonResult.success("insertPerson", FULL_NAME));
	}

	@Test
	@DisplayName("When a valid token is provided for the correct user " +
			"then a success result is returned with the correct data")
	void canCreateDerdeFromToken() {
		when(isimUserData.getInsz()).thenReturn(TOKEN_INSZ);
		when(tokenService.exchangeTokenForClaims(VALID_TOKEN)).thenReturn(createDerdeClaims());
		when(adminDomainService.findAdminDomainByIkp(IKP)).thenReturn(createOptionalAdminDomain());
		isimUserContextManager.setContext(isimUserData);

		ExternPersonService.CanCreateDerdeResult result = externPersonService.canCreateDerdeFromToken(VALID_TOKEN);

		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.getDerdeData().getOrganisationName()).isEqualTo(ORGANISATION_NAME);
		assertThat(result.getDerdeData().getNewEmailAdress()).isEqualTo(EMAIL_ADDRESS);
		assertThat(result.getDerdeData().getNewMobileNumber()).isEqualTo(MOBILE_NUMBER);
	}

	@Test
	@DisplayName("When the token does not belong to the logged in user then a failure is returned")
	void canCreateDerdeFromToken2() {
		when(isimUserData.getInsz()).thenReturn("someOtherInsz");
		when(tokenService.exchangeTokenForClaims(VALID_TOKEN)).thenReturn(createDerdeClaims());
		isimUserContextManager.setContext(isimUserData);

		ExternPersonService.CanCreateDerdeResult result = externPersonService.canCreateDerdeFromToken(VALID_TOKEN);

		assertThat(result.isSuccessful()).isFalse();
		assertThat(result.getErrorMessage()).isEqualTo("Uw inloggegevens komen niet overeen met het gevraagde profiel.");
	}

	@Test
	@DisplayName("When a valid token is provided for the correct user " +
			" but the ikp does not exists " +
			"then a failure result is returned with the correct message")
	void canCreateDerdeFromToken3() {
		when(isimUserData.getInsz()).thenReturn(TOKEN_INSZ);
		when(tokenService.exchangeTokenForClaims(VALID_TOKEN)).thenReturn(createDerdeClaims());
		when(adminDomainService.findAdminDomainByIkp(IKP)).thenReturn(Optional.empty());
		isimUserContextManager.setContext(isimUserData);

		ExternPersonService.CanCreateDerdeResult result = externPersonService.canCreateDerdeFromToken(VALID_TOKEN);

		assertThat(result.isSuccessful()).isFalse();
		assertThat(result.getErrorMessage()).isEqualTo("Er is een probleem met uw verzoek. Probeer het later opnieuw of contacteer uw administrator.");
	}

	@Test
	@DisplayName("When an invalid token is provided for the correct user " +
			"then a failure result is returned with the correct message")
	void canCreateDerdeFromToken4() {
		when(tokenService.exchangeTokenForClaims(VALID_TOKEN)).thenThrow(new TokenExchangeException(new TokenClientException(new HttpClientErrorException(HttpStatus.BAD_REQUEST))));

		ExternPersonService.CanCreateDerdeResult result = externPersonService.canCreateDerdeFromToken(VALID_TOKEN);

		assertThat(result.isSuccessful()).isFalse();
		assertThat(result.getErrorMessage()).isEqualTo("Deze link is niet (meer) geldig.");
	}

	@Test
	@DisplayName("When a valid token is provided for the correct user " +
			" but the token can't be exchanged " +
			"then a failure result is returned with the correct message")
	void canCreateDerdeFromToken5() {
		when(tokenService.exchangeTokenForClaims(VALID_TOKEN)).thenThrow(new TokenExchangeException(new RuntimeException("Boem!")));

		ExternPersonService.CanCreateDerdeResult result = externPersonService.canCreateDerdeFromToken(VALID_TOKEN);

		assertThat(result.isSuccessful()).isFalse();
		assertThat(result.getErrorMessage()).isEqualTo("Er is een probleem met uw verzoek. Probeer het later opnieuw of contacteer de servicelijn op het nummer 0800 30 700.");
	}

	@Test
	@DisplayName("When rrn already exists" +
			"Then a failure is returned with the correct message.")
	void createPerson1() {
		Dn dn = new Dn("blub");
		CreateDerdeCommand createDerdeCommand = mock(CreateDerdeCommand.class);
		when(createDerdeCommand.getInsz()).thenReturn("85081236549");
		when(createDerdeCommand.getOrganizationDn()).thenReturn(dn);
		when(personService.rrnExists("85081236549", dn)).thenReturn(true);

		ExternPersonService.CreatePersonResult result = externPersonService.createPerson(createDerdeCommand);

		assertThat(result.isSuccessful()).isFalse();
		assertThat(result.getMessageKey()).isEqualTo("errorCreate");
		assertThat(result.getMessageText()).isEqualTo("De nieuwe gebruiker kan niet aangemaakt worden omdat deze al bestaat binnen de opgegeven organisatie.");
	}

	@Test
	@DisplayName("When gebruikers is already in prullenbak for this organization" +
			"Then a failure is returned with the correct message.")
	void createPerson2() {
		Dn dn = new Dn("blub");
		CreateDerdeCommand createDerdeCommand = mock(CreateDerdeCommand.class);
		when(createDerdeCommand.getInsz()).thenReturn("85081236549");
		when(createDerdeCommand.getOrganizationDn()).thenReturn(dn);
		when(createDerdeCommand.getIkp()).thenReturn("10578861000");
		when(personService.rrnExists("85081236549", dn)).thenReturn(false);
		when(personService.getPersonDnInPrullenbakForRrnAndIkpNummer(any(), any())).thenReturn(new Dn("any"));

		ExternPersonService.CreatePersonResult result = externPersonService.createPerson(createDerdeCommand);

		assertThat(result.isSuccessful()).isFalse();
		assertThat(result.getMessageKey()).isEqualTo("errorCreate");
		assertThat(result.getMessageText()).isEqualTo("Je kan deze gebruiker niet zelf aanmaken. Bel hiervoor ons gratis nummer 0800 30 700.");
	}

	@Test
	@DisplayName("""
			WHEN creating derde from token WITH ikp that does not exists
			THEN RETURNS false WITH errormessage 'Fout bij het aanmaken van een persoon.'
			""")
	void createDerdeFromTokenWhereIkpDoesNotExists() {
		when(tokenService.exchangeTokenForClaims(VALID_TOKEN)).thenReturn(createDerdeClaims());
		isimUserContextManager.setContext(isimUserData);

		ExternPersonService.CreatePersonResult result = externPersonService.createDerdeFromToken(VALID_TOKEN);
		assertThat(result.isSuccessful()).isFalse();
		assertThat(result.getMessageText()).isEqualTo("Fout bij het aanmaken van een persoon.");
	}

	@Test
	@DisplayName("""
			WHEN creating derde from token WITHOUT roles
			THEN RETURNS true WITH messagekey 'insertPersonWithoutRoles'
			""")
	void createDerdeFromTokeWithoutRoles() {
		isimUserContextManager.setContext(isimUserData);

		when(tokenService.exchangeTokenForClaims(VALID_TOKEN)).thenReturn(createDerdeClaims());
		when(adminDomainService.findAdminDomainByIkp(IKP)).thenReturn(createOptionalAdminDomain(IKP));
		when(personService.findPersonByDn(any(), any())).thenReturn(Optional.of(new PersonObject()));

		ExternPersonService.CreatePersonResult result = externPersonService.createDerdeFromToken(VALID_TOKEN);
		assertThat(result.isSuccessful()).isTrue();
		assertThat(result.getMessageKey()).isEqualTo("insertPersonWithoutRoles");
	}

	private Optional<AdminDomainObject> createOptionalAdminDomain() {
		return Optional.of(createAdminDomainObject(null));
	}

	private Optional<AdminDomainObject> createOptionalAdminDomain(Ikp ikp) {
		return Optional.of(createAdminDomainObject(ikp));
	}

	private AdminDomainObject createAdminDomainObject(Ikp ikp) {
		AdminDomainObject adminDomain = new AdminDomainObject();
		adminDomain.setName(ORGANISATION_NAME);
		if (ikp != null) {
			adminDomain.setIkp(ikp);
		}
		return adminDomain;
	}

	private DerdeCreationClaims createDerdeClaims() {
		return DerdeCreationClaims.builder()
				.email(EMAIL_ADDRESS)
				.gsm(MOBILE_NUMBER)
				.ikp(IKP.getValue())
				.insz(TOKEN_INSZ)
				.naam("Meys")
				.voornaam("Joske")
				.telefoon("016236836")
				.toegangsrechten(Collections.singletonList("BOEIT-NI"))
				.build();
	}
}