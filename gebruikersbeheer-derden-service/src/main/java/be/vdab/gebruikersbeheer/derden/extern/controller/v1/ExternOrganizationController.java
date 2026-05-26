package be.vdab.gebruikersbeheer.derden.extern.controller.v1;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextHolder;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.CsvExportService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.exception.OrganizationNotFoundException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/extern/organization")
@RequiredArgsConstructor
@Slf4j
public class ExternOrganizationController extends ExternAbstractController {

	private final AdminDomainService adminDomainService;
	private final ApplicationProperties applicationProperties;
	private final CsvExportService csvExportService;
	private final IsimUserContextManager isimUserContextManager;

	/**
	 * Na inlog op de Tam => Controleren op aantal vestigingen en redirecten
	 * naar juiste pagina*
	 */
	@GetMapping()
	public String overview(Model model) {
		log.trace("overview");

		List<AdminDomainObject> adminDomains = adminDomainService.findAdminDomainsForAdministrator(IsimUserContextHolder.getContext().getPersonDn());

		// 1 vestiging
		if (adminDomains.size() == 1) {
			return "redirect:/extern/organization/" + adminDomains.getFirst().getDn().getGlobalId() + "/overview";
		} else {
			// Meerdere vestigingen
			model.addAttribute("adminDomains", adminDomains);
			return "/extern/organization/overzicht_main";
		}
	}

	@GetMapping(value = "{organization}/overview")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organization)")
	public String detail(@PathVariable String organization, Model model) {
		log.trace("detail organization: {}", organization);

		AdminDomainObject adminDomainObject = adminDomainService.findAdminDomainByDnWithRoles(applicationProperties.createAdminDomainDn(organization))
				.orElseThrow(() -> new OrganizationNotFoundException(organization));

		model.addAttribute("admindomain", adminDomainObject);

		return "redirect:/extern/organization/" + adminDomainObject.getDn().getGlobalId() + "/person/overzicht";
	}

	@GetMapping(value = "{organization}/exporteer")
	@PreAuthorize("@SecurityExpressions.canAccessOrganizationWithGlobalId(#organization)")
	public void exporteerGebruikers(@PathVariable String organization, HttpServletResponse response) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
		log.trace("exporteer {}", organization);

		Dn organizationDn = applicationProperties.createAdminDomainDn(organization);
		AdminDomainObject adminDomainObject = adminDomainService.findAdminDomainByDnWithRoles(organizationDn)
				.orElseThrow(() -> new OrganizationNotFoundException(organization));

		String fileName = "export-" + organization + ".csv";
		csvExportService.sendCsv(response, adminDomainObject.getIkp(), fileName);
	}
}
