package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.components.passwordchange.PasswordChangeClient;
import be.vdab.gebruikersbeheer.derden.exception.ITIMTemplateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccounServiceImplResetPasswordTest {

	private static final String VDAB_UID = "testje";
	private static final String EMAIL = "testje@vdab.be";

	@InjectMocks
	private AccountServiceImpl accountService;

	@Mock
	private PasswordChangeClient passwordChangeClient;

	@Test
	void canResetThePassword() {
		accountService.resetPassword(VDAB_UID, EMAIL);

		verify(passwordChangeClient).sendPasswordForgottenEmail(VDAB_UID, EMAIL);
	}

	@Test
	void mapsExceptions() {
		RuntimeException error = new RuntimeException("Error");
		doThrow(error).when(passwordChangeClient).sendPasswordForgottenEmail(VDAB_UID, EMAIL);

		assertThatThrownBy(() -> accountService.resetPassword(VDAB_UID, EMAIL))
				.isInstanceOf(ITIMTemplateException.class)
				.hasMessage("java.lang.RuntimeException occurred!")
				.hasCause(error);
	}
}