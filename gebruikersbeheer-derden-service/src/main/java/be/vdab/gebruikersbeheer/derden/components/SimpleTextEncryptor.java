package be.vdab.gebruikersbeheer.derden.components;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.jasypt.util.text.StrongTextEncryptor;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class SimpleTextEncryptor {

	private StrongTextEncryptor textEncryptor;
	private final ApplicationProperties applicationProperties;

	@PostConstruct
	void init() {
		textEncryptor = new StrongTextEncryptor();
		textEncryptor.setPassword(applicationProperties.getEncryptorSharedSecret());
	}

	public String encrypt(String text) {
		return Base64.getEncoder().withoutPadding().encodeToString(textEncryptor.encrypt(text).getBytes());
	}
}
