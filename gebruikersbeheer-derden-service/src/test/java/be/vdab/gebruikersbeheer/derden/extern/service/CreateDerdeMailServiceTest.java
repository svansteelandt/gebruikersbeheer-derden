package be.vdab.gebruikersbeheer.derden.extern.service;

import be.vdab.gebruikersbeheer.derden.domain.ConfirmNewDerdeCreation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateDerdeMailServiceTest {

	@Mock
	MailSender mailSender;

	CreateDerdeMailService createDerdeMailService;

	@BeforeEach
	void setUp() {
		createDerdeMailService = new CreateDerdeMailService(mailSender, "https://www.werkgevers.vdab.be");
	}

	@Test
	void sendBevestigNieuweToegangMijnVDABMail_validData_calledForOneRecipient() {
		createDerdeMailService.sendMail(sendCreateDerdeEmailCommand());

		verify(mailSender).sendMail(CreateDerdeMailCommandFactory.createDerdeMailCommandRegular());
		verify(mailSender).sendMail(CreateDerdeMailCommandFactory.createDerdeMailCommandTimCopy());
	}

	@Test
	void sendBevestigNieuweToegangMijnVDABMail_validData_calledForEveryRecipient() {
		Set<String> recipients = new HashSet<>();
		recipients.add("dries@vdab.be");
		recipients.add("dries@realdolmen.com");
		recipients.add("dries@desoftwarefabriek.be");
		ArgumentCaptor<ConfirmNewDerdeCreationMessage> captor = ArgumentCaptor.forClass(ConfirmNewDerdeCreationMessage.class);

		createDerdeMailService.sendMail(sendCreateDerdeEmailCommand(recipients));

		verify(mailSender, times(4)).sendMail(captor.capture());
		assertThat(captor.getAllValues())
				.extracting(message -> message.getOntvanger().getEmail(), ConfirmNewDerdeCreationMessage::getOnderwerp)
				.containsExactly(
						tuple("dries@vdab.be", "Bevestig nieuwe toegang Mijn VDAB"),
						tuple("dries@realdolmen.com", "Bevestig nieuwe toegang Mijn VDAB"),
						tuple("dries@desoftwarefabriek.be", "Bevestig nieuwe toegang Mijn VDAB"),
						tuple("derden.admin@vdab.be", "[TIM COPY] Bevestig nieuwe toegang Mijn VDAB")
				);
	}

	private ConfirmNewDerdeCreation sendCreateDerdeEmailCommand() {
		return sendCreateDerdeEmailCommand(Collections.singleton("dries@vdab.be"));
	}

	private ConfirmNewDerdeCreation sendCreateDerdeEmailCommand(Set<String> recipients) {
		return ConfirmNewDerdeCreation.builder()
				.newEmail("newEmail@mail.be")
				.firstNameNewUser("Dries")
				.lastNameNewUser("Thieren")
				.organisationName("VDAB")
				.token("eySchoonToken")
				.recipients(recipients)
				.tokenExpirationTime(LocalDateTime.now())
				.build();
	}
}