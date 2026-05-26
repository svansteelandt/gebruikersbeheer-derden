package be.vdab.gebruikersbeheer.derden.extern.service;

import be.vdab.gebruikersbeheer.derden.exception.MailSenderException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class MailSender {

	private static final String COMMUNICATIE_COMMAND = "VerstuurCommunicatie";
	private static final String MAIL_QUEUE = "communicatiebeheerMailbox.queue";
	private final String applicationName;
	private final JmsTemplate jmsTemplate;
	private final ObjectMapper objectMapper;

	public MailSender(@Value("${spring.application.name}") String applicationName, JmsTemplate jmsTemplate, ObjectMapper objectMapper) {
		this.applicationName = applicationName;
		this.jmsTemplate = jmsTemplate;
		this.objectMapper = objectMapper;
	}

	public void sendMail(ConfirmNewDerdeCreationMessage createDerdeMailMessage) {
		try {
			String json = objectMapper.writeValueAsString(createDerdeMailMessage);
			jmsTemplate.convertAndSend(MAIL_QUEUE, json, messagePostProcessor(json));
		} catch (Exception e) {
			throw new MailSenderException("Unable to send mailmessage.", e);
		}
	}

	private MessagePostProcessor messagePostProcessor(String json) {
		return message -> {
			message.setStringProperty("commandName", COMMUNICATIE_COMMAND);
			message.setStringProperty("idempotencykey", UUID.randomUUID().toString());
			message.setStringProperty("JMSXAppID", applicationName);
			message.setIntProperty("MAJOR_VERSION_KEY", 1);
			log.info("VerstuurCommunicatie verstuurd: {}, data: {}", message, json);
			return message;
		};
	}
}
