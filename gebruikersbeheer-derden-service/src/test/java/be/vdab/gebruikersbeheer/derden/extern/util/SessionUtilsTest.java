package be.vdab.gebruikersbeheer.derden.extern.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionUtilsTest {
	@Mock
	HttpServletRequest request;
	@Mock
	HttpSession httpSession;

	@Test
	void canSetAttribute() {
		when(request.getSession()).thenReturn(httpSession);
		SessionUtils.setSession(request, "test", "object");
		verify(httpSession).setAttribute("be_optis_extern_test", "object");
	}
}