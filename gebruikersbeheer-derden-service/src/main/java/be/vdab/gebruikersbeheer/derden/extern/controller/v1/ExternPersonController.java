package be.vdab.gebruikersbeheer.derden.extern.controller.v1;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.extension.FlashMap;
import be.vdab.gebruikersbeheer.derden.extern.command.CreatePersonCommandFactory;
import be.vdab.gebruikersbeheer.derden.extern.service.ExternPersonService;
import be.vdab.gebruikersbeheer.derden.extern.validator.PersonFormValidator;
import be.vdab.gebruikersbeheer.derden.extern.view.PersonCommand;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextHolder;
import be.vdab.gebruikersbeheer.derden.service.AccountService;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.MinimumAdminsReachedException;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.derden.util.LogSanitizer;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.exception.OrganizationNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/extern/organization/{organizationId}/person/")
public class ExternPersonController extends ExternAbstractController {

	private final AccountService accountService;
	private final PersonService personService;
	private final RoleService roleService;
	private final PersonFormValidator personFormValidator;
	private final AdminDomainService adminDomainService;
	private final ApplicationProperties applicationProperties;
	private final ExternPersonService externPersonService;
	private final CreatePersonCommandFactory createDerdeCommandFactory;

	@ModelAttribute("roles")
	public List<RoleObject> findRoles() {
		return roleService.findRoles();
	}

	@GetMapping("overzicht")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organizationId)")
	public String overview(@PathVariable String organizationId, Model model) {
		AdminDomainObject adminDomain = this.adminDomainService.findAdminDomainByDnWithRoles(this.applicationProperties.createAdminDomainDn(organizationId)).orElse(null);
		if (adminDomain == null) {
			return "redirect:/extern/organization/";
		}
		removeRolesThatAreNotDerdeExtern(adminDomain);

		model.addAttribute("admindomain", adminDomain);
		model.addAttribute("admindomainsize", adminDomainService.findAdminDomainsForAdministrator(IsimUserContextHolder.getContext().getPersonDn()).size());
		model.addAttribute("ingelogde", IsimUserContextHolder.getContext().getHoofdGebruikersnaam());
		model.addAttribute("persons", personService.findPersonsFromOrganization(adminDomain));

		List<PersonObject> personsInPrullenbak = personService.findPersonsInPrullenbakForIkpNummer(adminDomain.getIkp());
		removeVdabAdministratorName(personsInPrullenbak);
		model.addAttribute("personsinprullenbak", personsInPrullenbak);

		return "/extern/person/overzicht";
	}

	private void removeRolesThatAreNotDerdeExtern(AdminDomainObject adminDomain) {
		List<RoleObject> roles = adminDomain.getRoles().stream()
				.filter(role -> role.getTags().contains("DerdeExtern"))
				.collect(Collectors.toList());
		adminDomain.setRoles(roles);
	}

	private void removeVdabAdministratorName(List<PersonObject> persons) {
		persons.stream()
				.filter(person -> StringUtils.containsIgnoreCase(person.getDeleteDescription(), "VDAB"))
				.forEach(person -> person.setDeleteDescription("Verwijderd door VDAB"));
	}

	@GetMapping("{personId}/password/reset")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organizationId)")
	public String passwordreset(@PathVariable String organizationId,
	                            @PathVariable String personId) {
		log.debug("passwordreset {}", personId);

		AdminDomainObject adminDomainObject = this.adminDomainService.findAdminDomainByDnWithRoles(this.applicationProperties.createAdminDomainDn(organizationId)).orElse(null);
		if (adminDomainObject == null) {
			return "redirect:/extern/organization/";
		}

		Dn personDn = this.applicationProperties.createPersonDn(personId);
		PersonObject personObject = personService.findPersonByDn(personDn, adminDomainObject).orElse(null);
		if (personObject == null) {
			return "redirect:/extern/organization/";
		}

		String uid = "vdabvirtual".equalsIgnoreCase(personObject.getProfileName())
				? personObject.getUserId()
				: personObject.getVdabUid();

		accountService.resetPassword(uid, personObject.getEmailAddress());

		FlashMap.setInfoMessage("passwordReset", personObject.getFullName() + " zal een e-mail ontvangen met de instructies om een wachtwoord te kiezen.");

		return "redirect:/extern/organization/%s/person/%s/user_detail".formatted(adminDomainObject.getDn().getGlobalId(), personObject.getDn().getGlobalId());
	}

	@GetMapping("{personId}/user_detail")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organizationId)")
	public String view(@PathVariable String organizationId,
	                   @PathVariable String personId,
	                   HttpServletRequest request, Model model) {
		log.debug("view personId: {}", personId);

		AdminDomainObject adminDomain = this.adminDomainService.findAdminDomainByDnWithRoles(this.applicationProperties.createAdminDomainDn(organizationId)).orElse(null);
		if (adminDomain == null) {
			return "redirect:/extern/organization/";
		}

		PersonObject personObject = personService.findPersonByDn(this.applicationProperties.createPersonDn(personId), adminDomain).orElse(null);
		if (personObject == null) {
			return "redirect:/extern/organization/";
		}

		if (log.isTraceEnabled()) {
			if (personObject.getRoles() != null) {
				log.trace("ExternPersonController ROLES {}", personObject.getCommonName());
				personObject.getRoles().forEach(r -> log.trace("{} -> {}", r.getRoleName(), r.getHasRole()));
			} else {
				log.trace("ExternPersonController NO ROLES {}", personObject.getCommonName());
			}
		}

		model.addAttribute("admindomain", adminDomain);
		model.addAttribute("person", personObject);
		return "/extern/person/detail_gebruiker";
	}

	@GetMapping("/create")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organizationId)")
	public String create(@PathVariable String organizationId,
	                     Model model) {
		AdminDomainObject adminDomain = this.adminDomainService.findAdminDomainByDnWithRoles(this.applicationProperties.createAdminDomainDn(organizationId)).orElse(null);
		if (adminDomain == null) {
			return "redirect:/extern/organization/";
		}

		model.addAttribute("personCommand", new PersonCommand(adminDomain.getPossibleRolesForPerson()));
		return nieuweGebruikerScherm(adminDomain, model);
	}

	@PostMapping("/create")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organizationId)")
	public String insert(@PathVariable String organizationId,
	                     PersonCommand personCommand,
	                     BindingResult errors,
	                     Model model) {
		log.debug("insert {}", LogSanitizer.sanitize(personCommand.getPerson().toString()));
		AdminDomainObject adminDomainObject = getAdminDomain(organizationId);
		try {
			if (!externPersonService.validatePersonCommand(personCommand, errors, adminDomainObject.getDn(), adminDomainObject.getIkp())) {
				return nieuweGebruikerScherm(adminDomainObject, model);
			}

			ExternPersonService.CreatePersonResult result = externPersonService.createDerdeOrSendMail(createDerdeCommandFactory.getCreateDerdeCommand(personCommand, adminDomainObject));
			if (result.isSuccessful()) {
				FlashMap.setInfoMessage(result.getMessageKey(), result.getMessageText());
			} else {
				FlashMap.setErrorMessage(result.getMessageKey(), result.getMessageText());
			}

			AdminDomainObject reloadedAdminDomain = getAdminDomain(organizationId);
			return redirectToPersonOverzichtScherm(reloadedAdminDomain, model);
		} catch (Exception ex) {
			FlashMap.setErrorMessage("errorCreate", "Er is een fout opgetreden, probeer het later opnieuw.");
			log.error("Something went wrong when creating a derde user: {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
			return redirectToPersonOverzichtScherm(adminDomainObject, model);
		}
	}

	@GetMapping("{personId}/edit")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organizationId)")
	public String edit(@PathVariable String organizationId,
	                   @PathVariable String personId,
	                   Model model) {
		AdminDomainObject adminDomain = getAdminDomain(organizationId);
		PersonObject person = this.personService.findPersonByDn(this.applicationProperties.createPersonDn(personId), adminDomain).orElse(null);

		if (person == null) {
			return "redirect:/extern/search/new";
		}

		model.addAttribute("personCommand", new PersonCommand(person));
		return wijzigGegevensScherm(adminDomain, model);
	}

	@PostMapping("{personId}/edit")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organizationId)")
	public String update(@PathVariable String organizationId,
	                     @PathVariable String personId,
	                     PersonCommand personCommand, BindingResult result,
	                     Model model) {
		log.debug("update {}", LogSanitizer.sanitize(personCommand.toString()));

		AdminDomainObject adminDomainObject = getAdminDomain(organizationId);
		PersonObject person = this.personService.findPersonByDn(this.applicationProperties.createPersonDn(personId), adminDomainObject).orElse(null);
		if (person == null) {
			return "redirect:/extern/organization/";
		}

		PersonObject updatePerson = personCommand.getPerson();
		personFormValidator.validate(updatePerson, result);
		if (result.hasErrors()) {
			return wijzigGegevensScherm(adminDomainObject, model);
		}

		if (updatePerson.getDn() == null && person.getDn() != null) {
			updatePerson.setDn(person.getDn());
		}

		personService.updatePerson(updatePerson);

		return "redirect:/extern/organization/%s/person/%s/user_detail".formatted(adminDomainObject.getDn().getGlobalId(), person.getDn().getGlobalId());
	}

	@GetMapping("{personId}/delete")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organizationId)")
	public String delete(@PathVariable String organizationId,
	                     @PathVariable String personId,
	                     Model model) {
		log.debug("delete personId {}", personId);

		AdminDomainObject adminDomain = this.adminDomainService.findAdminDomainByDnWithRoles(this.applicationProperties.createAdminDomainDn(organizationId)).orElse(null);
		if (adminDomain == null) {
			return "redirect:/extern/organization/";
		}

		PersonObject personObject = personService.findPersonByDn(this.applicationProperties.createPersonDn(personId), adminDomain).orElse(null);

		if (personObject == null) {
			return "redirect:/organization/";
		}

		model.addAttribute("admindomain", adminDomain);
		model.addAttribute("person", personObject);

		return "/extern/person/verwijder";
	}

	@PostMapping("{personId}/delete")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organizationId)")
	public String deleteAction(@PathVariable String organizationId,
	                           @PathVariable String personId,
	                           Model model) {
		log.debug("delete {} {}", organizationId, personId);

		AdminDomainObject adminDomainObject = this.adminDomainService.findAdminDomainByDn(this.applicationProperties.createAdminDomainDn(organizationId)).orElse(null);
		if (adminDomainObject == null) {
			return "redirect:/extern/organization/";
		}

		PersonObject personObject = this.personService.findPersonByDn(this.applicationProperties.createPersonDn(personId), adminDomainObject).orElse(null);
		if (personObject == null) {
			return "redirect:/extern/organization/";
		}

		try {
			personService.deletePerson(adminDomainObject, personObject);

			FlashMap.setInfoMessage("deletePerson", personObject.getFullName());

			AdminDomainObject reloadedAdminDomainObject = this.adminDomainService.findAdminDomainByDn(adminDomainObject.getDn()).orElseThrow(OrganizationNotFoundException::new);
			return redirectToPersonOverzichtScherm(reloadedAdminDomainObject, model);
		} catch (MinimumAdminsReachedException e) {
			FlashMap.setWarningMessage("errorMessageMinAdminsOnDeleteUser", "De gebruiker werd niet verwijderd aangezien er minimum 1 admin per vestiging moet zijn.");
		}

		return redirectToPersonOverzichtScherm(adminDomainObject, model);
	}

	/**
	 * Wijzig rechten Persoon
	 */
	@GetMapping("{personId}/gebruiker_wijzig_rechten")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organizationId)")
	public String rights(@PathVariable String organizationId,
	                     @PathVariable String personId,
	                     Model model) {
		log.debug("rights");

		AdminDomainObject adminDomainObject = this.adminDomainService.findAdminDomainByDnWithRoles(this.applicationProperties.createAdminDomainDn(organizationId)).orElse(null);
		if (adminDomainObject == null) {
			return "redirect:/extern/organization/";
		}

		PersonObject person = this.personService.findPersonByDn(this.applicationProperties.createPersonDn(personId), adminDomainObject).orElse(null);
		if (person == null) {
			return "redirect:/extern/organization/";
		}
		if (IsimUserContextHolder.getContext() != null) {
			model.addAttribute("ingelogde", IsimUserContextHolder.getContext().getHoofdGebruikersnaam());
		}

		model.addAttribute("admindomain", adminDomainObject);
		model.addAttribute("personCommand", new PersonCommand(person));
		return "/extern/person/gebruiker_wijzig_rechten";
	}

	@PostMapping("{personId}/gebruiker_wijzig_rechten")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organizationId)")
	public String rights(@PathVariable String organizationId,
	                     @PathVariable String personId,
	                     PersonCommand personCommand,
	                     Model model) {
		log.debug("rights personCommand: {}", LogSanitizer.sanitize(personCommand.getPerson().getUserId()));

		PersonObject personObject = personCommand.getPerson();

		if (personObject.getDn() == null) {
			log.debug("person {} heeft geen dn (stap 1)", personId);
		}

		AdminDomainObject adminDomainObjectBeforeManipulation = getAdminDomain(organizationId);

		roleService.addAndRemoveRoles(personObject, adminDomainObjectBeforeManipulation);

		AdminDomainObject adminDomainObject = adminDomainService.findAdminDomainByDnWithRoles(adminDomainObjectBeforeManipulation.getDn())
				.orElseThrow(OrganizationNotFoundException::new);
		model.addAttribute("admindomain", adminDomainObject);

		if (personObject.getDn() == null) {
			log.debug("person {} heeft geen dn (stap 2)", personId);
		}

		Dn personDn = this.applicationProperties.createPersonDn(personId);
		personService.updatePersonCaches(personDn, adminDomainObject);

		return "redirect:/extern/organization/%s/person/%s/user_detail".formatted(organizationId, personId);
	}

	@GetMapping("{personId}/restore")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organizationId)")
	public String restorePerson(@PathVariable String organizationId,
	                            @PathVariable String personId,
	                            Model model) {
		PersonObject person = this.personService.findPersonByDn(this.applicationProperties.createPersonDn(personId), null).orElse(null);
		AdminDomainObject adminDomain = this.adminDomainService.findAdminDomainByDn(this.applicationProperties.createAdminDomainDn(organizationId)).orElse(null);
		if (!isValidPersonInAdminDomain(person, adminDomain)) {
			return personInOrganizationOverviewUrl(organizationId);
		}

		model.addAttribute("person", person);
		model.addAttribute("admindomain", adminDomain);
		return "/extern/person/restore";
	}

	@PostMapping("{personId}/restore")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organizationId)")
	public String restorePersonAction(@PathVariable String organizationId,
	                                  @PathVariable String personId) {
		findValidPersonInAdminDomain(personId, organizationId)
				.filter(personService::restorePerson)
				.ifPresent(validPerson -> {
					sendEmailWithNewPassword(validPerson);
					FlashMap.setInfoMessage("restorePerson", validPerson.getFullName());
				});

		return personInOrganizationOverviewUrl(organizationId);
	}

	private Optional<PersonObject> findValidPersonInAdminDomain(String personId, String organizationId) {
		PersonObject person = this.personService.findPersonByDn(this.applicationProperties.createPersonDn(personId), null).orElse(null);
		AdminDomainObject adminDomain = this.adminDomainService.findAdminDomainByDn(this.applicationProperties.createAdminDomainDn(organizationId)).orElse(null);
		if (isValidPersonInAdminDomain(person, adminDomain)) {
			return Optional.of(person);
		} else {
			return Optional.empty();
		}
	}


	private boolean isValidPersonInAdminDomain(PersonObject person, AdminDomainObject adminDomain) {
		return person != null && adminDomain != null && Objects.equals(person.getIkp(), adminDomain.getIkp());
	}

	private void sendEmailWithNewPassword(PersonObject person) {
		if (StringUtils.isNotBlank(person.getEmailAddress())) {
			accountService.resetPassword(person.getUserId(), person.getEmailAddress().trim());
			FlashMap.setInfoMessage("passwordReset", person.getFullName() + " zal een e-mail ontvangen met de instructies om een wachtwoord te kiezen.");
		} else {
			FlashMap.setErrorMessage("passwordReset", "Er kan geen nieuw wachtwoord voor '" + person.getFullName() + "' aangevraagd worden aangezien er geen e-mailadres gekend is.");
		}
	}


	private String nieuweGebruikerScherm(AdminDomainObject adminDomainObject, Model model) {
		model.addAttribute("admindomain", adminDomainObject);
		return "/extern/person/nieuwe_gebruiker";
	}

	private String wijzigGegevensScherm(AdminDomainObject adminDomainObject, Model model) {
		model.addAttribute("admindomain", adminDomainObject);
		return "/extern/person/gebruiker_wijzig_gegevens";
	}

	private String redirectToPersonOverzichtScherm(AdminDomainObject adminDomainObject, Model model) {
		model.addAttribute("admindomain", adminDomainObject);
		return personInOrganizationOverviewUrl(adminDomainObject.getDn().getGlobalId());
	}

	private AdminDomainObject getAdminDomain(String organizationId) {
		return this.adminDomainService
				.findAdminDomainByDnWithRoles(this.applicationProperties.createAdminDomainDn(organizationId))
				.orElseThrow(OrganizationNotFoundException::new);
	}

	private static String personInOrganizationOverviewUrl(String organizationId) {
		return "redirect:/extern/organization/" + organizationId + "/person/overzicht";
	}
}
