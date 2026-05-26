package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper.TaakMapper;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.TaakDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.TaakSummaryDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.GoedkeurenCvsRolCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.GoedkeurenWebCursusCommand;
import be.vdab.gebruikersbeheer.derden.components.SimpleTextEncryptor;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.TaakType;
import be.vdab.gebruikersbeheer.derden.domain.WorkFlowProcessObject;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.TaakNotFoundException;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.TaskService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import com.ibm.itim.workflow.model.ActivityResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TakenRestControllerTest {

	@InjectMocks
	TakenRestController takenRestController;

	@Mock
	AdminDomainService adminDomainService;

	@Mock
	ApplicationProperties applicationProperties;

	@Mock
	TaskService taskService;

	@Mock
	TaakMapper taakMapper;

	@Mock
	SimpleTextEncryptor simpleTextEncryptor;

	@Test
	void getWorkOrderTaken() {
		var adminDomainObject = createAdminDomainObject();
		when(adminDomainService.findAdminDomainByDnWithRoles(any(Dn.class))).thenReturn(Optional.of(adminDomainObject));
		when(applicationProperties.createAdminDomainDn(anyString())).thenReturn(adminDomainObject.getDn());

		var workFlowProcessObject = createWorkFlowProcessObject();
		when(taskService.getAssignmentsWorkorders(adminDomainObject.getIkp())).thenReturn(Collections.singletonList(workFlowProcessObject));

		var taak = new TaakSummaryDto(
				workFlowProcessObject.getId(),
				workFlowProcessObject.getActivityName(),
				workFlowProcessObject.getAdminDomainName(),
				workFlowProcessObject.getRequestedFor(),
				workFlowProcessObject.getRequestedBy(),
				workFlowProcessObject.getRequestDateAsDate());

		when(taakMapper.map(anyList())).thenReturn(List.of(taak));

		assertThat(takenRestController.getTaken(TaakType.WORKORDER, adminDomainObject.getDn().toString())).usingRecursiveComparison().isEqualTo(List.of(taak));
	}

	@Test
	void getRFITaken() {
		var adminDomainObject = createAdminDomainObject();
		when(adminDomainService.findAdminDomainByDnWithRoles(any(Dn.class))).thenReturn(Optional.of(adminDomainObject));
		when(applicationProperties.createAdminDomainDn(anyString())).thenReturn(adminDomainObject.getDn());

		var workFlowProcessObject = createWorkFlowProcessObject();
		when(taskService.getAssignmentsRFI(adminDomainObject.getIkp())).thenReturn(Collections.singletonList(workFlowProcessObject));

		var taak = new TaakSummaryDto(
				workFlowProcessObject.getId(),
				workFlowProcessObject.getActivityName(),
				workFlowProcessObject.getAdminDomainName(),
				workFlowProcessObject.getRequestedFor(),
				workFlowProcessObject.getRequestedBy(),
				workFlowProcessObject.getRequestDateAsDate());

		when(taakMapper.map(anyList())).thenReturn(List.of(taak));

		assertThat(takenRestController.getTaken(TaakType.RFI, adminDomainObject.getDn().toString())).usingRecursiveComparison().isEqualTo(List.of(taak));
	}

	@Test
	void getTaakById() {
		var workFlowProcessObject = createWorkFlowProcessObject();

		var taak = new TaakDto(
				workFlowProcessObject.getId(),
				workFlowProcessObject.getRequestedFor(),
				workFlowProcessObject.getAdminDomainObject().getAddress(),
				workFlowProcessObject.getAdminDomainObject().getIkpIntern(),
				workFlowProcessObject.getPersonObject().getEmailAddress(),
				workFlowProcessObject.getPersonObject().getUserId());

		when(taskService.getAssignmentById(anyString())).thenReturn(workFlowProcessObject);
		when(taakMapper.map(workFlowProcessObject)).thenReturn(taak);

		assertThat(List.of(takenRestController.getTaak(anyString()))).usingRecursiveComparison().isEqualTo(List.of(taak));
	}

	@Test
	void goedkeurenRFI() {
		var workFlowProcessObject = createWorkFlowProcessObject();

		var mlpRole = "MLP_PRESTATIES";
		var webCursussen = List.of("cursus1", "cursus2");
		var goedkeurenWebCursusCommand = new GoedkeurenWebCursusCommand(mlpRole, webCursussen);

		var activityResult = new ActivityResult();

		when(taskService.getAssignmentById(anyString())).thenReturn(workFlowProcessObject);
		when(taakMapper.mapCommandToActivityResult(goedkeurenWebCursusCommand)).thenReturn(activityResult);

		takenRestController.goedkeurenRFI(workFlowProcessObject.getId(), goedkeurenWebCursusCommand);

		verify(taskService).approveWorkFlowProcess(workFlowProcessObject.getId(), activityResult);
	}

	@Test
	void goedkeurenOnbestaandeRFITaak() {
		var mlpRole = "MLP_PRESTATIES";
		var webCursussen = List.of("cursus1", "cursus2");
		var goedkeurenWebCursusCommand = new GoedkeurenWebCursusCommand(mlpRole, webCursussen);

		assertThatThrownBy(() -> takenRestController.goedkeurenRFI("321", goedkeurenWebCursusCommand))
				.isInstanceOf(TaakNotFoundException.class);
	}

	@Test
	void goedkeurenWorkorder() {
		var workFlowProcessObject = createWorkFlowProcessObject();

		when(applicationProperties.getIdentitytoolApplicationUrl()).thenReturn("https://identitytool-xxx.vdab.be/identitytool");
		when(taskService.getAssignmentById(anyString())).thenReturn(workFlowProcessObject);
		when(simpleTextEncryptor.encrypt(anyString())).thenReturn("encrypted");

		var resultaat = takenRestController.goedkeurenWorkorder(workFlowProcessObject.getId(), new GoedkeurenCvsRolCommand(true, "Taak is goedgekeurd"));
		assertThat(resultaat.verwijderUrl()).isNotEmpty().startsWith("%s/zoek-mlp-gebruiker".formatted(applicationProperties.getIdentitytoolApplicationUrl()));
	}

	@Test
	void goedkeurenOnbestaandeWorkorderTaak() {
		var command = new GoedkeurenCvsRolCommand(true, "Taak is goedgekeurd");

		assertThatThrownBy(() -> takenRestController.goedkeurenWorkorder("321", command))
				.isInstanceOf(TaakNotFoundException.class);
	}

	@Test
	void goedkeurenWorkorderMetOnbestaandePersoon() {
		var workFlowProcessObject = createWorkFlowProcessObject();
		workFlowProcessObject.setPersonObject(null);

		var command = new GoedkeurenCvsRolCommand(true, "Taak is goedgekeurd");

		when(taskService.getAssignmentById(anyString())).thenReturn(workFlowProcessObject);

		assertThatThrownBy(() -> takenRestController.goedkeurenWorkorder("321", command))
				.isInstanceOf(PersonNotFoundException.class);
	}

	private WorkFlowProcessObject createWorkFlowProcessObject() {
		var personObject = new PersonObject();
		personObject.setEmailAddress("user@vdab.be");
		personObject.setNationalNumber("1000000000");
		personObject.setUserId("JOSKE");

		var workFlowProcessObject = new WorkFlowProcessObject();
		workFlowProcessObject.setId("123456");
		workFlowProcessObject.setActivityName("Activity Name");
		workFlowProcessObject.setRequestedBy("REQUEST_USER_BY");
		workFlowProcessObject.setRequestedFor("REQUEST_USER_FOR");
		workFlowProcessObject.setRequestDate(new Date(System.currentTimeMillis()));

		workFlowProcessObject.setAdminDomainObject(createAdminDomainObject());
		workFlowProcessObject.setAdminDomainName(workFlowProcessObject.getAdminDomainObject().getName());
		workFlowProcessObject.setPersonObject(personObject);

		return workFlowProcessObject;
	}

	private AdminDomainObject createAdminDomainObject() {
		var adminDomainObject = new AdminDomainObject();
		adminDomainObject.setDn(new Dn("erglobalid=123456"));
		adminDomainObject.setName("Bedrijf X");
		adminDomainObject.setStreet("Keizerslaan 1");
		adminDomainObject.setPostalcode("1000");
		adminDomainObject.setCity("Brussel");
		adminDomainObject.setIkp(Ikp.of(203000L));

		return adminDomainObject;
	}
}
