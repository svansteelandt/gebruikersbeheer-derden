package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.TaakDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.TaakSummaryDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.GoedkeurenCvsRolCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.GoedkeurenWebCursusCommand;
import be.vdab.gebruikersbeheer.derden.domain.WorkFlowProcessObject;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.workflow.model.ActivityResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TaakMapper {

	public List<TaakSummaryDto> map(List<WorkFlowProcessObject> assignments) {
		return assignments.stream()
				.map(this::toTaak)
				.toList();
	}

	public TaakDto map(WorkFlowProcessObject workFlowProcessObject) {
		return new TaakDto(workFlowProcessObject.getId(),
				workFlowProcessObject.getRequestedFor(),
				workFlowProcessObject.getAdminDomainObject() != null ? workFlowProcessObject.getAdminDomainObject().getAddress() : null,
				workFlowProcessObject.getAdminDomainObject() != null ? workFlowProcessObject.getAdminDomainObject().getIkpIntern() : null,
				workFlowProcessObject.getPersonObject() != null ? workFlowProcessObject.getPersonObject().getEmailAddress() : null,
				workFlowProcessObject.getPersonObject() != null ? workFlowProcessObject.getPersonObject().getUserId() : null);
	}

	public ActivityResult mapCommandToActivityResult(GoedkeurenWebCursusCommand goedkeurenWebCursusCommand) {
		return new ActivityResult(ActivityResult.SUBMITED, mapCommandToActivityResultDetail(goedkeurenWebCursusCommand));
	}

	private List<Map<String, AttributeValue>> mapCommandToActivityResultDetail(GoedkeurenWebCursusCommand goedkeurenWebCursusCommand) {
		Map<String, AttributeValue> mapAttributes = new HashMap<>();

		mapAttributes.put("weblerenCursus", new AttributeValue("weblerenCursus", goedkeurenWebCursusCommand.webCursusCodes()));

		// CVS Role
		var weblerenRoles = goedkeurenWebCursusCommand.mlpRolCode();
		mapAttributes.put("weblerenRoles", new AttributeValue("weblerenRoles", weblerenRoles));

		return List.of(mapAttributes);
	}

	public WorkFlowProcessObject map(GoedkeurenCvsRolCommand goedkeurenCvsRolCommand) {
		var taak = new WorkFlowProcessObject();
		taak.setApprove(goedkeurenCvsRolCommand.approved());
		taak.setComment(goedkeurenCvsRolCommand.opmerking());

		return taak;
	}

	public TaakSummaryDto toTaak(WorkFlowProcessObject assignment) {
		return new TaakSummaryDto(assignment.getId(),
				assignment.getActivityName(),
				assignment.getAdminDomainName(),
				assignment.getRequestedBy(),
				assignment.getRequestedFor(),
				assignment.getRequestDateAsDate());
	}
}
