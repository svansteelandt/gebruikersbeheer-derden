package be.vdab.gebruikersbeheer.derden.components.passwordchange;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordChangeClientImplTest {

    @Mock
	RestTemplate restTemplate;

	@Mock
	ApplicationProperties applicationProperties;

    @InjectMocks
	PasswordChangeClientImpl passwordChangeClient;

    @Test
    void sendPasswordForgottenEmailWhenSuccessFull() {
		String uid = "test,intern";
		String email = "test@vdab.be";
		when(applicationProperties.getPasswordChangeUrl()).thenReturn("url");
		passwordChangeClient.sendPasswordForgottenEmail(uid, email);
		verify(restTemplate).getForEntity("url/service/change/forgottenmail?username={uid}&email={emailAdres}", String.class, uid, email);
    }
}