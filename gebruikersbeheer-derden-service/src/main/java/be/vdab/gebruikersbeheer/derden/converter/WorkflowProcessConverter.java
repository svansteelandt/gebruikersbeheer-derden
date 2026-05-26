package be.vdab.gebruikersbeheer.derden.converter;

import be.vdab.gebruikersbeheer.derden.domain.WorkFlowProcessObject;
import be.vdab.gebruikersbeheer.util.isim.domain.WorkflowProcess;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WorkflowProcessConverter implements Converter<WorkflowProcess, WorkFlowProcessObject> {

	private final PersonConverter personConverter;
	private final AdminDomainConverter adminDomainConverter;

	public WorkflowProcessConverter(PersonConverter personConverter, AdminDomainConverter adminDomainConverter) {
		this.personConverter = personConverter;
		this.adminDomainConverter = adminDomainConverter;
	}

	public WorkFlowProcessObject convert(WorkflowProcess workflowProcess) {
		WorkFlowProcessObject workFlowProcessObject = new WorkFlowProcessObject();

		workFlowProcessObject.setId(workflowProcess.getId());
		workFlowProcessObject.setSubject(workflowProcess.getSubject());
		workFlowProcessObject.setRequestedFor(workflowProcess.getRequestedFor());
		workFlowProcessObject.setRequestedBy(workflowProcess.getRequestedBy());
		workFlowProcessObject.setActivityName(workflowProcess.getActivityName());
		workFlowProcessObject.setActivityDesignId(workflowProcess.getActivityDesignId());
		workFlowProcessObject.setType(workflowProcess.getType());
		workFlowProcessObject.setIkpNummer(workflowProcess.getIkp());
		workFlowProcessObject.setAdminDomainName(workflowProcess.getAdminDomainName());
		workFlowProcessObject.setRequestDate(workflowProcess.getRequestDate());
		if (workflowProcess.getPerson() != null) {
			workFlowProcessObject.setPersonObject(personConverter.convert(workflowProcess.getPerson()));
		}
		if (workflowProcess.getAdminDomain() != null) {
			workFlowProcessObject.setAdminDomainObject(adminDomainConverter.convert(workflowProcess.getAdminDomain()));
		}

		return workFlowProcessObject;
	}
}
