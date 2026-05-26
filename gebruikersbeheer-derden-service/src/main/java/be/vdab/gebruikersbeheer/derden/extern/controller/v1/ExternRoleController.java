package be.vdab.gebruikersbeheer.derden.extern.controller.v1;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.RoleNotFoundException;
import be.vdab.gebruikersbeheer.derden.extern.view.RoleCommand;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextHolder;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.derden.util.LogSanitizer;
import be.vdab.gebruikersbeheer.util.exception.OrganizationNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/extern/organization/{organization}/role/")
@SessionAttributes(types = RoleCommand.class)
@Slf4j
@RequiredArgsConstructor
public class ExternRoleController extends ExternAbstractController {

	private final RoleService roleService;
	private final PersonService personService;
	private final AdminDomainService adminDomainService;
	private final ApplicationProperties applicationProperties;
	private final IsimUserContextManager isimUserContextManager;

	@GetMapping("{roleId}")
	public String edit(@PathVariable String organization, @PathVariable String roleId, Model model) {
		RoleObject roleObject = roleService.findRoleByGlobalId(roleId).orElseThrow(RoleNotFoundException::new);

		Optional<AdminDomainObject> optionalAdminDomain = adminDomainService.findAdminDomainByDnWithRoles(applicationProperties.createAdminDomainDn(organization));
		if (optionalAdminDomain.isEmpty()) {
			return "redirect:/extern/organization/";
		}
		AdminDomainObject adminDomain = optionalAdminDomain.get();

		if (IsimUserContextHolder.getContext() != null) {
			model.addAttribute("ingelogde", IsimUserContextHolder.getContext().getHoofdGebruikersnaam());
		}

		model.addAttribute("admindomain", adminDomain);
		model.addAttribute("roleCommand", new RoleCommand(roleObject, personService.findPersonsFromOrganizationDnWithRoleWithPending(adminDomain.getDn(), roleObject)));
		model.addAttribute("administrators_maxcount", applicationProperties.getMaxDomainAdmins());
		model.addAttribute("administrators_mincount", applicationProperties.getMinDomainAdmins());

		return "/extern/role/rol_detail";
	}

	@PostMapping("{roleId}")
	public String doEdit(@PathVariable String organization,
	                     @PathVariable String roleId,
	                     RoleCommand roleCommand,
	                     Model model) {
		log.debug("doEdit {} {} {}", organization, roleId, LogSanitizer.sanitize(roleCommand.toString()));

		AdminDomainObject adminDomainObject = adminDomainService.findAdminDomainByDnWithRoles(applicationProperties.createAdminDomainDn(organization))
				.orElseThrow(OrganizationNotFoundException::new);
		roleService.changePersonRoleChangeList(roleCommand.getPersonObject(), roleCommand.getRoleObject(), adminDomainObject);

		if (roleCommand.getPersonObject() != null) {
			roleCommand.getPersonObject().stream().filter(PersonObject::isChanged).forEach(o -> {
				log.debug("person {} changed {} hasrole {}", o.getUserId(), o.isChanged(), o.getHasRole());
				personService.updatePersonCaches(o.getDn(), adminDomainObject);
			});
		}
		model.addAttribute("admindomain", adminDomainService.findAdminDomainByDnWithRoles(adminDomainObject.getDn()));
		return "redirect:/extern/organization/" + adminDomainObject.getDn().getGlobalId() + "/person/overzicht";
	}
}
