package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.converter.WorkflowProcessConverter;
import be.vdab.gebruikersbeheer.derden.domain.IsimUserData;
import be.vdab.gebruikersbeheer.derden.domain.WorkFlowProcessObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextHolder;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.service.cache.CacheService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.isim.client.IsimUtils;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.domain.WorkflowProcess;
import be.vdab.gebruikersbeheer.util.repository.TaskDao;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.workflow.model.ActivityResult;
import com.ibm.itim.ws.model.todo.WSRFIWrapper;
import com.ibm.itim.ws.services.service.ArrayOfTns1WSAttribute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

	private final TaskDao taskDao;
	private final IsimWsClient isimWsClient;
	private final WorkflowProcessConverter workflowProcessConverter;
	private final CacheService cacheService;
	private final IsimUserContextManager isimUserContextManager;

	@Override
	public List<WorkFlowProcessObject> getAssignmentsWorkorders(Ikp ikpNummer) {
		return getAssignmentsWorkflow(ikpNummer, "WO");
	}

	@Override
	public List<WorkFlowProcessObject> getAssignmentsRFI(Ikp ikpNummer) {
		return getAssignmentsWorkflow(ikpNummer, "RI");
	}

	private List<WorkFlowProcessObject> getAssignmentsWorkflow(Ikp ikpNummer, String type) {
		Dn isimAccountDn = Optional.ofNullable(IsimUserContextHolder.getContext())
				.map(IsimUserData::getIsimAccountDn)
				.orElse(null);

		return taskDao.getAssignmentsWorkflow(isimAccountDn, type, ikpNummer).stream()
				.map(workflowProcessConverter::convert)
				.collect(Collectors.toList());
	}

	@Override
	public WorkFlowProcessObject getAssignmentById(String id) {
		log.trace("getAssignmentById {}", id);

		return taskDao.getAssignmentWorkflowById(id)
				.map(workflowProcessConverter::convert)
				.orElse(null);
	}

	@Override
	public List<String> getPendingRoles(String uid) {
		return taskDao.getPendingRoles(uid);
	}

	@Override
	public void approveWorkFlowProcess(String id, WorkFlowProcessObject workFlowProcessObject) {
		if (log.isTraceEnabled()) {
			log.trace("approveWorkFlowProcess {} workflowprocessobject: {}", id, workFlowProcessObject.toString());
		}

		IsimWsSession session = isimUserContextManager.getSession();
		isimWsClient.approveOrReject(session, Long.parseLong(id), workFlowProcessObject.getApprove(), workFlowProcessObject.getComment());

		if (workFlowProcessObject.getPersonObject() != null) {
			this.cacheService.deleteFromPersonCaches(workFlowProcessObject.getPersonObject().getDn());
		}
	}

	@Override
	public void approveWorkFlowProcess(String id, ActivityResult activityResult) {
		log.trace("approveWorkFlowProcess {} activitiyresult: {}", id, activityResult);

		IsimWsSession session = isimUserContextManager.getSession();
		WSRFIWrapper rfiWrapper = isimWsClient.getRFI(session, Long.parseLong(id));
		WSRFIWrapper updatedRfiWrapper = updateValues(activityResult, rfiWrapper);
		if (updatedRfiWrapper != null) {
			isimWsClient.submitRFI(session, updatedRfiWrapper);
		}
	}

	@Override
	public void removeWorkFlowProcessesForUser(Dn userDn) {
		List<WorkflowProcess> workflowProcesses = taskDao.getAssignmentsWorkflowRelatingToUser(userDn.toString());
		if (workflowProcesses.isEmpty()) return;

		IsimWsSession session = isimUserContextManager.getSession();
		workflowProcesses.forEach(w -> isimWsClient.approveOrReject(session, Long.parseLong(w.getId()), false, "User is deleted"));
	}

	@Override
	public List<WorkflowProcess> getWorkFlowProcessesForUser(Dn userDn) {
		return taskDao.getAssignmentsWorkflowRelatingToUser(userDn.toString());
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private WSRFIWrapper updateValues(ActivityResult activityResult, WSRFIWrapper rfiWrapper) {
		List<?> values = activityResult.getDetail();

		if (values == null || values.size() != 1) return null;
		Object value = values.getFirst();

		if (!(value instanceof Map)) return null;
		Map<?, ?> map = (Map) value;

		ArrayOfTns1WSAttribute wsAttributes = rfiWrapper.getWsAttrValues();
		for (Object key : map.keySet()) {
			Object mapValue = map.get(key);
			if (mapValue instanceof AttributeValue attributeValue) {
				IsimUtils.modifyAttribute(wsAttributes, key.toString(), attributeValue.getValues());
			}
		}

		rfiWrapper.setWsAttrValues(wsAttributes);
		return rfiWrapper;
	}
}
