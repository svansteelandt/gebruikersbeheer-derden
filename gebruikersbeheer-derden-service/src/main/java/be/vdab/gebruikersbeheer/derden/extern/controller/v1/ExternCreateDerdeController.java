package be.vdab.gebruikersbeheer.derden.extern.controller.v1;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.extern.service.ExternPersonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/extern/person/create")
@RequiredArgsConstructor
public class ExternCreateDerdeController {

	private final ExternPersonService externPersonService;
	private final ApplicationProperties applicationProperties;

	@GetMapping(params = {"token"})
	@PreAuthorize("isAuthenticated()")
	public String getCreateDerdeViaToken(@RequestParam String token, Model model) {
		ExternPersonService.CanCreateDerdeResult result = externPersonService.canCreateDerdeFromToken(token);
		if (result.isSuccessful()) {
			model.addAttribute("newMobileNumber", result.getDerdeData().getNewMobileNumber());
			model.addAttribute("newEmailAddress", result.getDerdeData().getNewEmailAdress());
			model.addAttribute("organisationName", result.getDerdeData().getOrganisationName());
			model.addAttribute("token", token);
			model.addAttribute("cancelLink", applicationProperties.getWerkgeversUrl());
			return "/extern/person/create/confirmation";
		}

		model.addAttribute("errorMessage", result.getErrorMessage());
		return "/extern/person/create/failure";
	}

	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public String postCreateDerdeViaToken(@Valid @ModelAttribute TokenForm tokenForm, Model model) {
		ExternPersonService.CreatePersonResult result = externPersonService.createDerdeFromToken(tokenForm.getToken());
		if (result.isSuccessful()) {
			return "/extern/person/create/success";
		}else {
			model.addAttribute("errorMessage", result.getMessageText());
			return "/extern/person/create/failure";
		}
	}

	@Data
	protected static final class TokenForm {
		@NotNull
		String token;
	}
}
