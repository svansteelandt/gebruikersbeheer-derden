package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.converter.WorkflowProcessConverter;
import be.vdab.gebruikersbeheer.derden.domain.IsimUserData;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.WorkFlowProcessObject;
import be.vdab.gebruikersbeheer.derden.monitoring.MonitoringService;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.service.cache.CacheService;
import be.vdab.gebruikersbeheer.derden.util.isim.support.IsimSessionService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.domain.WorkflowProcess;
import be.vdab.gebruikersbeheer.util.repository.TaskDao;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.workflow.model.ActivityResult;
import com.ibm.itim.ws.model.todo.WSRFIWrapper;
import com.ibm.itim.ws.services.service.ArrayOfTns1WSAttribute;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

	TaskServiceImpl taskService;

	@Mock
	WorkFlowProcessObject workFlowProcessObject;

	@Mock
	IsimWsClient isimWsClient;

	@Mock
	IsimWsSession isimWsSession;

	@Mock
	ActivityResult activityResult;

	@Mock
	WSRFIWrapper wsrfiWrapper;

	@Mock
	PersonObject person;

	@Mock
	TaskDao taskDao;

	@Mock
	WorkflowProcessConverter workflowProcessConverter;

	@Mock
	private IsimSessionService isimSessionService;

	@Mock
	private MonitoringService monitoringService;

	@Mock
	private CacheService cacheService;

	private IsimUserContextManager isimUserContextManager;

	@BeforeEach
	void setUp() {
		isimUserContextManager = new IsimUserContextManager(isimSessionService, monitoringService);
		taskService = new TaskServiceImpl(taskDao, isimWsClient, workflowProcessConverter, cacheService, isimUserContextManager);
	}

	@AfterEach
	void cleanUp() {
		isimUserContextManager.clearSessions();
		isimUserContextManager.setContext(null);
	}

	@Test
	void approveWorkFlowProcess() {
		when(isimSessionService.getAdminSession()).thenReturn(isimWsSession);
		when(workFlowProcessObject.getComment()).thenReturn("com");
		when(workFlowProcessObject.getApprove()).thenReturn(true);
		when(person.getDn()).thenReturn(new Dn("d"));
		when(workFlowProcessObject.getPersonObject()).thenReturn(person);

		taskService.approveWorkFlowProcess("1", workFlowProcessObject);

		verify(isimWsClient).approveOrReject(isimWsSession, Long.parseLong("1"), workFlowProcessObject.getApprove(), workFlowProcessObject.getComment());
		verify(cacheService).deleteFromPersonCaches(person.getDn());
	}

	@Test
	void approveWorkFlowProcessMetActivityResult() {
		Map<String, AttributeValue> attributes = new HashMap<>();
		attributes.put("1", new AttributeValue("test", Collections.emptyList()));

		when(isimSessionService.getAdminSession()).thenReturn(isimWsSession);
		when(activityResult.getDetail()).thenReturn(List.of(attributes));
		when(isimWsClient.getRFI(isimWsSession, Long.parseLong("1"))).thenReturn(wsrfiWrapper);
		when(wsrfiWrapper.getWsAttrValues()).thenReturn(new ArrayOfTns1WSAttribute());

		taskService.approveWorkFlowProcess("1", activityResult);

		verify(isimWsClient).getRFI(isimWsSession, Long.parseLong("1"));
		verify(isimWsClient).submitRFI(isimWsSession, wsrfiWrapper);
	}

	@Test
	void getAssignmentsWorkorders() {
		// when(isimSessionService.getAdminSession()).thenReturn(isimWsSession);
		Ikp ikp = Ikp.of("203000");
		WorkflowProcess workflowProcess = mock(WorkflowProcess.class);

		when(taskDao.getAssignmentsWorkflow(getIsimAccountDn(), "WO", ikp)).thenReturn(List.of(workflowProcess));
		when(workflowProcessConverter.convert(any(WorkflowProcess.class))).thenReturn(workFlowProcessObject);

		List<WorkFlowProcessObject> workFlowProcessObjects = taskService.getAssignmentsWorkorders(ikp);
		assertThat(workFlowProcessObjects).hasSize(1);
	}

	@Test
	void getAssignmentsRFI() {
		Ikp ikp = Ikp.of("203000");
		WorkflowProcess workflowProcess = mock(WorkflowProcess.class);

		when(taskDao.getAssignmentsWorkflow(getIsimAccountDn(), "RI", ikp)).thenReturn(List.of(workflowProcess));
		when(workflowProcessConverter.convert(any(WorkflowProcess.class))).thenReturn(workFlowProcessObject);

		List<WorkFlowProcessObject> workFlowProcessObjects = taskService.getAssignmentsRFI(ikp);
		assertThat(workFlowProcessObjects).hasSize(1);
	}

	@Test
	@DisplayName("""
			WHEN approving workflow with detail value
			THEN the rfi is submitted to ISIM
			""")
	void approveWorkFlowWithCorrectInput() {
		Map<String, AttributeValue> attributes = new HashMap<>();
		attributes.put("1", new AttributeValue("test", Collections.emptyList()));

		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(isimWsClient.getRFI(isimWsSession, Long.parseLong("1"))).thenReturn(wsrfiWrapper);
		when(wsrfiWrapper.getWsAttrValues()).thenReturn(new ArrayOfTns1WSAttribute());
		when(activityResult.getDetail()).thenReturn(List.of(attributes));

		taskService.approveWorkFlowProcess("1", activityResult);

		verify(isimWsClient).submitRFI(any(IsimWsSession.class), any(WSRFIWrapper.class));
	}

	@Test
	@DisplayName("""
			WHEN approving workflow without detail value
			THEN the rfi is never submitted to ISIM
			""")
	void approveWorkFlowWithoutDetailValue() {
		when(activityResult.getDetail()).thenReturn(null);

		taskService.approveWorkFlowProcess("1", activityResult);

		verify(isimWsClient, never()).submitRFI(any(IsimWsSession.class), any(WSRFIWrapper.class));
	}

	@Test
	@DisplayName("""
			WHEN approving workflow with multiple detail values
			THEN the rfi is never submitted to ISIM
			""")
	void approveWorkFlowWithMultipleDetailValues() {
		when(activityResult.getDetail()).thenReturn(List.of(Collections.emptyMap(), Collections.emptyMap()));

		taskService.approveWorkFlowProcess("1", activityResult);

		verify(isimWsClient, never()).submitRFI(any(IsimWsSession.class), any(WSRFIWrapper.class));
	}

	@Test
	@DisplayName("""
			WHEN getting pending roles of user
			THEN returns pending roles of the user
			""")
	void testGetPendingRoles() {
		String uid = "UID";
		List<String> roles = List.of("Role1", "Role2");
		when(taskDao.getPendingRoles(uid)).thenReturn(roles);

		assertThat(taskService.getPendingRoles(uid)).isEqualTo(roles);
	}

	@Test
	@DisplayName("""
			WHEN getting the workflow processes of a user
			THEN returns all related workflow processes of a user
			""")
	void testGetWorkFlowProcessesForUser() {
		long workFlowProcessId = 123456L;
		Dn userDn = new Dn("erglobalid=123,ou=0,ou=people");

		WorkflowProcess workflowProcess = mock(WorkflowProcess.class);
		when(workflowProcess.getId()).thenReturn(Long.toString(workFlowProcessId));
		when(taskDao.getAssignmentsWorkflowRelatingToUser(userDn.toString())).thenReturn(List.of(workflowProcess));

		List<WorkflowProcess> workflowProcesses = taskService.getWorkFlowProcessesForUser(userDn);

		assertThat(workflowProcesses)
				.hasSize(1)
				.first()
				.extracting(WorkflowProcess::getId)
				.isEqualTo(Long.toString(workFlowProcessId));
	}

	@Test
	@DisplayName("""
			WHEN removing the workflow process of a user
			THEN all related workflow processes are rejected with the reason 'User is deleted'
			""")
	void testRemoveWorkFlowProcessesForUser() {
		long workFlowProcessId = 123456L;
		Dn userDn = new Dn("erglobalid=123,ou=0,ou=people");

		WorkflowProcess workflowProcess = mock(WorkflowProcess.class);
		when(workflowProcess.getId()).thenReturn(Long.toString(workFlowProcessId));
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(taskDao.getAssignmentsWorkflowRelatingToUser(userDn.toString())).thenReturn(List.of(workflowProcess));

		taskService.removeWorkFlowProcessesForUser(userDn);

		verify(isimWsClient).approveOrReject(isimWsSession, workFlowProcessId, false, "User is deleted");
	}

	@Test
	@DisplayName("""
			WHEN attempting to remove workflow processes for a user who has NONE
			THEN no rejection calls are made to the ISIM service
			""")
	void testRemovingWorkFlowProcessesForUserWithoutWorkFlowProcesses() {
		Dn userDn = new Dn("erglobalid=123,ou=0,ou=people");

		when(taskDao.getAssignmentsWorkflowRelatingToUser(userDn.toString())).thenReturn(List.of());

		taskService.removeWorkFlowProcessesForUser(userDn);

		verify(isimWsClient, never()).approveOrReject(eq(isimWsSession), anyLong(), anyBoolean(), anyString());
	}

	private Dn getIsimAccountDn() {
		Dn isimAccountDn = new Dn("isimAccount");
		isimUserContextManager.setContext(IsimUserData.builder().isimAccountDn(isimAccountDn).build());

		return isimAccountDn;
	}
}