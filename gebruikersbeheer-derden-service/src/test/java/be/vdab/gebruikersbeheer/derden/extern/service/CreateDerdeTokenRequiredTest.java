package be.vdab.gebruikersbeheer.derden.extern.service;


import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.ConfirmNewDerdeCreation;
import be.vdab.gebruikersbeheer.derden.domain.DerdeCreationToken;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonStatus;
import be.vdab.gebruikersbeheer.derden.extern.command.CreateDerdeCommand;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.derden.service.TokenService;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDerdeTokenRequiredTest {

	static final String FULL_NAME = "Dries Thieren";
	static final String INSZ = "nietbelangrijk";
	static final String VALID_TOKEN = "eySchoonTokenSeg";

	@Mock
	PersonService personService;
	@Mock
	TokenService tokenService;
	@Mock
	CreateDerdeMailService createDerdeMailService;

	@InjectMocks
	ExternPersonService externPersonService;

	@BeforeEach
	void beforeEach() {
		lenient().when(personService.findPersonByDn(any(), any())).thenReturn(Optional.of(new PersonObject()));
	}

	@Test
	@DisplayName("When a new derde has no other accounts " +
			"then no token is required for creation")
	void createPerson() {
		whenNewDerdeHasNoOtherAccounts();

		ExternPersonService.CreatePersonResult result = externPersonService.createDerdeOrSendMail(createDerdeCommand("driest@nieuw.be"));

		assertThat(result).isEqualTo(ExternPersonService.CreatePersonResult.success("insertPersonWithoutRoles", FULL_NAME));
	}

	@Test
	@DisplayName("When a new derde has other accounts " +
			"and at least one of them has the same email address as the new one " +
			"then no token is required for creation")
	void createPerson2() {
		whenNewDerdeHasOtherAccountsWithEmails("dries@vdab.be", "dthieren@vdab.be");

		ExternPersonService.CreatePersonResult result = externPersonService.createDerdeOrSendMail(createDerdeCommand("dries@vdab.be"));

		assertThat(result).isEqualTo(ExternPersonService.CreatePersonResult.success("insertPersonWithoutRoles", FULL_NAME));
	}

	@Test
	@DisplayName("When a new derde has other accounts " +
			"but none of them has the same email address as the new one " +
			"then a token is required for creation")
	void createPerson3() {
		whenNewDerdeHasOtherAccountsWithEmails("dries@vdab.be", "dthieren@vdab.be");
		when(tokenService.getDerdeCreationToken(any())).thenReturn(new DerdeCreationToken(VALID_TOKEN, LocalDateTime.now()));

		ExternPersonService.CreatePersonResult result = externPersonService.createDerdeOrSendMail(createDerdeCommand("dries@hacker.cn"));

		assertThat(result).isEqualTo(ExternPersonService.CreatePersonResult.success("errorCreate", "Dries Thieren is al gekend als gebruiker en zal een e-mail met instructies ontvangen om dit nieuwe account aan te maken."));
		verifyEmailIsSentToRecipients("dries@vdab.be", "dthieren@vdab.be", "dries@hacker.cn");
	}

	@Test
	@DisplayName("When a new derde has other accounts " +
			"but all of them are in the prullenbak " +
			"then no token is required for creation")
	void createPerson4() {
		whenNewDerdeHasOtherPrullenbakAccountsWithEmails("dries@vdab.be");

		ExternPersonService.CreatePersonResult result = externPersonService.createDerdeOrSendMail(createDerdeCommand("dries@hacker.cn"));

		assertThat(result).isEqualTo(ExternPersonService.CreatePersonResult.success("insertPersonWithoutRoles", FULL_NAME));
	}

	private void verifyEmailIsSentToRecipients(String... emailAddresses) {
		ArgumentCaptor<ConfirmNewDerdeCreation> captor = ArgumentCaptor.forClass(ConfirmNewDerdeCreation.class);
		verify(createDerdeMailService).sendMail(captor.capture());
		assertThat(captor.getValue().getRecipients()).containsExactlyInAnyOrder(emailAddresses);
	}

	private void whenNewDerdeHasNoOtherAccounts() {
		whenNewDerdeHasOtherAccountsWithEmails();
	}

	private void whenNewDerdeHasOtherAccountsWithEmails(String... otherEmails) {
		when(personService.findPersons(any()))
				.thenReturn(Arrays.stream(otherEmails).map(this::personWithEmail).collect(Collectors.toList()));
	}

	private void whenNewDerdeHasOtherPrullenbakAccountsWithEmails(String... otherEmails) {
		when(personService.findPersons(any()))
				.thenReturn(Arrays.stream(otherEmails).map(this::inactivePersonWithEmail).collect(Collectors.toList()));
	}

	@NotNull
	private CreateDerdeCommand createDerdeCommand(String email) {
		CreateDerdeCommand createDerdeCommand = mock(CreateDerdeCommand.class);
		when(createDerdeCommand.getInsz()).thenReturn(INSZ);
		when(createDerdeCommand.getFullName()).thenReturn(FULL_NAME);
		lenient().when(createDerdeCommand.getEmailAddress()).thenReturn(email);
		lenient().when(createDerdeCommand.getPersonToInsert()).thenReturn(new PersonObject());
		lenient().when(createDerdeCommand.getOrganization()).thenReturn(mock(AdminDomainObject.class));
		return createDerdeCommand;
	}

	@NotNull
	private PersonObject personWithEmail(String emailAddress) {
		PersonObject person = new PersonObject();
		person.setStatus(PersonStatus.ACTIVE);
		person.setEmailAddress(emailAddress);
		return person;
	}

	@NotNull
	private PersonObject inactivePersonWithEmail(String emailAddress) {
		PersonObject person = new PersonObject();
		person.setStatus(PersonStatus.SUSPENDED);
		person.setEmailAddress(emailAddress);
		return person;
	}
}