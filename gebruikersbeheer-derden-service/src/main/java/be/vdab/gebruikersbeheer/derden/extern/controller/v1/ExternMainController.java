package be.vdab.gebruikersbeheer.derden.extern.controller.v1;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class ExternMainController {

	private final ApplicationProperties applicationProperties;

	@GetMapping(value = {"/", "/extern"})
	public String overview(HttpServletRequest request) {
		log.debug("overview");

		return "home";
	}

	@GetMapping(value = "/home")
	public String home(HttpServletRequest request) {
		log.debug("home");

		return "home";
	}

	@GetMapping(value = "afmelden")
	public void afmelden(HttpServletResponse response) {
		log.debug("afmelden extern");

		try {
			SecurityContextHolder.clearContext();

			String logoutUrl = this.applicationProperties.getLogoutWerkgeversUrl();

			if (logoutUrl == null) {
				log.error("logoutUrl: {}", logoutUrl);
				return;
			}

			response.sendRedirect(response.encodeRedirectURL(logoutUrl));
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}