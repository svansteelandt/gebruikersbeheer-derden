package be.vdab.gebruikersbeheer.derden.extern.service;

import be.vdab.gebruikersbeheer.derden.domain.ConfirmNewDerdeCreation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;


@Service
public class CreateDerdeMailService {

	private static final String EMAIL_DERDEN_ADMIN = "derden.admin@vdab.be";
	private final MailSender mailSender;
	private final String gebruikersBeheerDerdenUrl;

	public CreateDerdeMailService(MailSender mailSender, @Value("${service.werkgevers.extern.url}") String gebruikersBeheerDerdenUrl) {
		this.mailSender = mailSender;
		this.gebruikersBeheerDerdenUrl = gebruikersBeheerDerdenUrl;
	}

	public void sendMail(ConfirmNewDerdeCreation confirmNewDerdeCreation) {
		for (String email : confirmNewDerdeCreation.getRecipients()) {
			mailSender.sendMail(confirmNewDerdeCreationMessageRegular(confirmNewDerdeCreation, email));
		}
		mailSender.sendMail(confirmNewDerdeCreationMessageTimCopy(confirmNewDerdeCreation));
	}

	private ConfirmNewDerdeCreationMessage confirmNewDerdeCreationMessageRegular(ConfirmNewDerdeCreation command, String email) {
		return confirmNewDerdeCreationMessage(command, "", command.getFirstNameNewUser(), command.getLastNameNewUser(), email);
	}

	private ConfirmNewDerdeCreationMessage confirmNewDerdeCreationMessageTimCopy(ConfirmNewDerdeCreation command) {
		return confirmNewDerdeCreationMessage(command, "[TIM COPY] ", "Derden", "Admin", EMAIL_DERDEN_ADMIN);
	}

	private ConfirmNewDerdeCreationMessage confirmNewDerdeCreationMessage(ConfirmNewDerdeCreation command, String onderwerpPrefix, String emailVoornaam, String emailNaam, String email) {
		return ConfirmNewDerdeCreationMessage.builder()
				.ontvanger(ConfirmNewDerdeCreationMessage.Ontvanger.builder()
						.email(email)
						.voornaam(emailVoornaam)
						.naam(emailNaam)
						.build())
				.communicatieType("IAM_DERDEN_AANMAKEN_NIEUWE_GEBRUIKER_VIA_TOKEN")
				.kanalen(Collections.singletonList("EMAIL"))
				.onderwerp(onderwerpPrefix + "Bevestig nieuwe toegang Mijn VDAB")
				.businessdata(ConfirmNewDerdeCreationMessage.Businessdata.builder()
						.iamMailInfo(ConfirmNewDerdeCreationMessage.IamMailInfo.builder()
								.email_nieuw(command.getNewEmail())
								.geldigheidsduur_token(daysValid(command))
								.naam_organisatie(command.getOrganisationName())
								.url(url(command.getToken()))
								.voornaam_nieuwe_gebruiker(command.getFirstNameNewUser())
								.build())
						.build())
				.build();
	}

	private String url(String token) {
		return gebruikersBeheerDerdenUrl + "/gebruikersbeheer-derden/extern/person/create?token=" + token;
	}

	private String daysValid(ConfirmNewDerdeCreation command) {
		return String.valueOf(Duration.between(LocalDateTime.now(), command.getTokenExpirationTime()).toDays());
	}
}
