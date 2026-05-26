package be.vdab.gebruikersbeheer.derden.components.passwordchange;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PasswordChangeClientImpl implements PasswordChangeClient {

	private final RestTemplate restTemplate;
	private final ApplicationProperties applicationProperties;

	public void sendPasswordForgottenEmail(String vdabUid, String emailAdres){
		log.debug("sendPasswordForgottenEmail uid: {} email: {}", vdabUid, emailAdres);

		restTemplate.getForEntity(this.applicationProperties.getPasswordChangeUrl() + "/service/change/forgottenmail?username={uid}&email={emailAdres}", String.class, vdabUid, emailAdres);
	}
}
