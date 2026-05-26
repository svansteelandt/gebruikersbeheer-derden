package be.vdab.gebruikersbeheer.derden.extern.service;

import be.vdab.gebruikersbeheer.derden.exception.MailSenderException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.InvalidDestinationException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.MessagePostProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailSenderTest {

	private static final ConfirmNewDerdeCreationMessage CREATE_DERDE_MAIL_COMMAND = CreateDerdeMailCommandFactory.createDerdeMailCommandRegular();

	@Mock
	JmsTemplate jmsTemplate;

	MailSender mailSender;

	@BeforeEach
	void setUp() {
		mailSender = new MailSender("applicationName", jmsTemplate, new ObjectMapper());
	}

	@Test
	@DisplayName("When sending a mail to an invalid destination " +
			"then a correct exception is thrown")
	void sendMail1() {
		doThrow(InvalidDestinationException.class).when(jmsTemplate).convertAndSend(anyString(), any(MessageCreator.class), any(MessagePostProcessor.class));

		assertThatThrownBy(() -> mailSender.sendMail(CREATE_DERDE_MAIL_COMMAND)).isInstanceOf(MailSenderException.class);
	}

	@Test
	@DisplayName("When sending a mail throws a RuntimeException " +
			"then a correct exception is thrown")
	void sendMail2() {
		doThrow(RuntimeException.class).when(jmsTemplate).convertAndSend(anyString(), any(MessageCreator.class), any(MessagePostProcessor.class));

		assertThatThrownBy(() -> mailSender.sendMail(CREATE_DERDE_MAIL_COMMAND)).isInstanceOf(MailSenderException.class);
	}

	@Test
	@DisplayName("When a valid command is given " +
			"then a mail is sent with the correct data")
	void sendMail3() {
		mailSender.sendMail(CREATE_DERDE_MAIL_COMMAND);
		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

		verify(jmsTemplate).convertAndSend(eq("communicatiebeheerMailbox.queue"), messageCaptor.capture(), any(MessagePostProcessor.class));

		assertThat(messageCaptor.getValue()).isNotNull();
	}


}