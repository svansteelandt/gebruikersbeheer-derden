package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper.TaakMapper;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GoedkeurenCvsRolResultaatDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.TaakDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.TaakSummaryDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.GoedkeurenCvsRolCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.GoedkeurenWebCursusCommand;
import be.vdab.gebruikersbeheer.derden.components.SimpleTextEncryptor;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.TaakType;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.TaakNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.TaskService;
import be.vdab.gebruikersbeheer.derden.util.UrlUtil;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/ui/taken")
@Slf4j
@RequiredArgsConstructor
public class TakenRestController {

	private final TaskService taskService;
	private final AdminDomainService adminDomainService;
	private final ApplicationProperties applicationProperties;
	private final TaakMapper taakMapper;
	private final SimpleTextEncryptor simpleTextEncryptor;

	@GetMapping
	public List<TaakSummaryDto> getTaken(@RequestParam(value = "type") TaakType taakType,
	                                     @RequestParam(value = "vestigingId", required = false) String vestigingGlobalId) throws VestigingNietGevondenException {
		var ikp = bepaalIkpVanVestiging(vestigingGlobalId);

		var assignments = switch (taakType) {
			case WORKORDER -> taskService.getAssignmentsWorkorders(ikp);
			case RFI -> taskService.getAssignmentsRFI(ikp);
		};

		return taakMapper.map(assignments);
	}

	private Ikp bepaalIkpVanVestiging(String vestigingGlobalId) {
		Ikp ikp = null;
		if (vestigingGlobalId != null) {
			var organizationDn = applicationProperties.createAdminDomainDn(vestigingGlobalId);
			var vestiging = adminDomainService.findAdminDomainByDnWithRoles(organizationDn).orElseThrow(() -> new VestigingNietGevondenException(vestigingGlobalId));
			ikp = vestiging.getIkp();
		}
		return ikp;
	}

	@GetMapping("{id}")
	public TaakDto getTaak(@PathVariable("id") String taakId) {
		return taakMapper.map(taskService.getAssignmentById(taakId));
	}

	@PutMapping(value = "{id}", params = {"type=RFI"})
	@PreAuthorize("@SecurityExpressions.hasRole('" + RoleNames.ROL_CVS_RFI + "') and @SecurityExpressions.isBeheerderDerden()")
	public void goedkeurenRFI(@PathVariable("id") String taakId, @Valid @RequestBody GoedkeurenWebCursusCommand command) {
		var workFlowProcessObject = taskService.getAssignmentById(taakId);
		if (workFlowProcessObject == null) {
			throw new TaakNotFoundException();
		}

		taskService.approveWorkFlowProcess(taakId, taakMapper.mapCommandToActivityResult(command));
	}

	@PutMapping(value = "{id}", params = {"type=WORKORDER"})
	@PreAuthorize("@SecurityExpressions.hasRole('" + RoleNames.ROL_CVS_APPROVAL + "') and @SecurityExpressions.isBeheerderDerden()")
	public GoedkeurenCvsRolResultaatDto goedkeurenWorkorder(@PathVariable("id") String taakId, @Valid @RequestBody GoedkeurenCvsRolCommand command) {
		var workFlowProcessObject = taskService.getAssignmentById(taakId);
		if (workFlowProcessObject == null) {
			throw new TaakNotFoundException();
		}
		if (workFlowProcessObject.getPersonObject() == null) {
			throw new PersonNotFoundException();
		}

		taskService.approveWorkFlowProcess(taakId, taakMapper.map(command));

		var encryptedInsz = "{enc}" + simpleTextEncryptor.encrypt(workFlowProcessObject.getPersonObject().getNationalNumber());

		return new GoedkeurenCvsRolResultaatDto(UriComponentsBuilder.fromUriString(applicationProperties.getIdentitytoolApplicationUrl())
				.path("/zoek-mlp-gebruiker")
				.queryParam("insz", UrlUtil.urlEncode(encryptedInsz))
				.queryParam("delete", "true")
				.build()
				.toUriString());
	}
}
