package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.WorkFlowProcessObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.isim.domain.WorkflowProcess;
import com.ibm.itim.workflow.model.ActivityResult;

import java.util.List;

public interface TaskService {

	List<WorkFlowProcessObject> getAssignmentsWorkorders(Ikp ikpNummer);

	List<WorkFlowProcessObject> getAssignmentsRFI(Ikp ikpNummer);

	WorkFlowProcessObject getAssignmentById(String id);

	List<String> getPendingRoles(String uid);

	void approveWorkFlowProcess(String id, WorkFlowProcessObject workFlowProcessObject);

	void approveWorkFlowProcess(String id, ActivityResult activityResult);

	void removeWorkFlowProcessesForUser(Dn dn);

	List<WorkflowProcess> getWorkFlowProcessesForUser(Dn userDn);
}
