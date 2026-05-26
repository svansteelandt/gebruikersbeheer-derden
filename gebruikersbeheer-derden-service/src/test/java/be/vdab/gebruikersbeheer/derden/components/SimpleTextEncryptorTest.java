package be.vdab.gebruikersbeheer.derden.components;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleTextEncryptorTest {

	@InjectMocks
	SimpleTextEncryptor simpleTextEncryptor;

	@Mock
	ApplicationProperties applicationProperties;

	@BeforeEach
	void setup() {
		when(applicationProperties.getEncryptorSharedSecret()).thenReturn("s3cret");
		simpleTextEncryptor.init();
	}

	@Test
	void encrypt() {
		String result = simpleTextEncryptor.encrypt("someInput");
		assertThatCode(() -> Base64.getDecoder().decode(result)).doesNotThrowAnyException();
	}
}