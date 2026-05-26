package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.TaakSummaryDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.GoedkeurenCvsRolCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.GoedkeurenWebCursusCommand;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.WorkFlowProcessObject;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.workflow.model.ActivityResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaakMapperTest {

	private static final String BEDRIJF_ID = "123456";
	private static final String ACTIVITY_NAME = "ActivityName X";
	private static final String BEDRIJF_NAME = "Bedrijf X";
	private static final String REQUESTED_FOR = "Requested For";
	private static final String REQUESTED_BY = "Requested By";
	private static final Date REQUESTED_DATE = new Date(System.currentTimeMillis());

	@InjectMocks
	TaakMapper taakMapper;

	@Mock
	WorkFlowProcessObject workFlowProcessObject;

	@Test
	@DisplayName("""
			GIVEN valid WorkFlowProcessObject
			WHEN map is called
			THEN valid TaakDto is returned
			""")
	void mapWorkFlowProcessObject() {
		var adminDomainObject = mock(AdminDomainObject.class);
		when(adminDomainObject.getAddress()).thenReturn("Straat 1 1000 Brussel");
		when(adminDomainObject.getIkpIntern()).thenReturn("203-0");

		var personObject = mock(PersonObject.class);
		when(personObject.getEmailAddress()).thenReturn("user@vdab.be");
		when(personObject.getUserId()).thenReturn("JOSKE");

		when(workFlowProcessObject.getId()).thenReturn(BEDRIJF_ID);
		when(workFlowProcessObject.getRequestedFor()).thenReturn(REQUESTED_FOR);
		when(workFlowProcessObject.getAdminDomainObject()).thenReturn(adminDomainObject);
		when(workFlowProcessObject.getPersonObject()).thenReturn(personObject);

		var taakDto = taakMapper.map(workFlowProcessObject);
		assertThat(taakDto.id()).isEqualTo(workFlowProcessObject.getId());
		assertThat(taakDto.klant()).isEqualTo(workFlowProcessObject.getRequestedFor());
		assertThat(taakDto.adres()).isEqualTo(adminDomainObject.getAddress());
		assertThat(taakDto.ikp()).isEqualTo(adminDomainObject.getIkpIntern());
		assertThat(taakDto.email()).isEqualTo(personObject.getEmailAddress());
		assertThat(taakDto.userId()).isEqualTo(personObject.getUserId());
	}

	@Test
	@DisplayName("""
			GIVEN valid WorkFlowProcessObject
			WHEN map is called
			THEN valid TaakSummaryDto is returned
			""")
	void mapWorkFlowProcessObjectsToSummary() {
		var workFlowProcessObject = createWorkFlowProcessObject();
		var mappedTaakSummary = taakMapper.toTaak(workFlowProcessObject);

		assertThat(mappedTaakSummary.id()).isEqualTo(BEDRIJF_ID);
		assertThat(mappedTaakSummary.onderwerp()).isEqualTo(ACTIVITY_NAME);
		assertThat(mappedTaakSummary.vestigingNaam()).isEqualTo(BEDRIJF_NAME);
		assertThat(mappedTaakSummary.aangevraagdDoor()).isEqualTo(REQUESTED_BY);
		assertThat(mappedTaakSummary.aangevraagdVoor()).isEqualTo(REQUESTED_FOR);
		assertThat(mappedTaakSummary.aangevraagdDatum()).isEqualTo(REQUESTED_DATE);
	}

	@Test
	@DisplayName("""
			GIVEN valid List of WorkFlowProcessObject
			WHEN map is called
			THEN valid List of TaakSummaryDto is returned
			""")
	void mapWorkFlowProcessObjectsToSummaries() {
		var workFlowProcessObject = createWorkFlowProcessObject();
		var taakSummary = new TaakSummaryDto(
				workFlowProcessObject.getId(),
				workFlowProcessObject.getActivityName(),
				workFlowProcessObject.getAdminDomainName(),
				workFlowProcessObject.getRequestedBy(),
				workFlowProcessObject.getRequestedFor(),
				workFlowProcessObject.getRequestDateAsDate()
		);

		assertThat(taakMapper.map(List.of(workFlowProcessObject))).usingRecursiveComparison().isEqualTo(List.of(taakSummary));
	}

	@Test
	@DisplayName("""
			GIVEN valid GoedkeurenCvsRolCommand
			WHEN map is called
			THEN valid WorkFlowProcessObject is returned
			""")
	void mapGoedkeurenCvsRolCommand() {
		var goedkeurenCvsRolCommand = new GoedkeurenCvsRolCommand(true, "Taak is goedgekeurd");

		var workFlowProcessObject = taakMapper.map(goedkeurenCvsRolCommand);

		assertThat(workFlowProcessObject.getApprove()).isEqualTo(goedkeurenCvsRolCommand.approved());
		assertThat(workFlowProcessObject.getComment()).isEqualTo(goedkeurenCvsRolCommand.opmerking());
	}

	@Test
	@DisplayName("""
			GIVEN valid GoedkeurenWebCursusCommand
			WHEN map is mapCommandToActivityResult
			THEN valid ActivityResult is returned
			""")
	void mapCommandToActivityResult() {
		String mlpRole = "MLP_PRESTATIES";
		List<String> webCursussen = Arrays.asList("cursus1", "cursus2");
		var goedkeurenWebCursusCommand = new GoedkeurenWebCursusCommand(mlpRole, webCursussen);
		var result = taakMapper.mapCommandToActivityResult(goedkeurenWebCursusCommand);

		assertThat(result.getSummary()).isEqualTo(ActivityResult.SUBMITED);
		assertThat(result.getDetail()).isNotEmpty().hasSize(1);

		@SuppressWarnings("unchecked")
		Map<String, AttributeValue> mapAttributes = (Map<String, AttributeValue>) result.getDetail().getFirst();
		assertThat(mapAttributes.containsKey("weblerenRoles")).isTrue();
		assertThat(mapAttributes.containsKey("weblerenCursus")).isTrue();
		assertThat(mapAttributes.get("weblerenRoles").getValues()).isEqualTo((List.of(mlpRole)));
		assertThat(mapAttributes.get("weblerenCursus").getValues()).isEqualTo(webCursussen);
	}

	private static WorkFlowProcessObject createWorkFlowProcessObject() {
		var workFlowProcessObject = new WorkFlowProcessObject();
		workFlowProcessObject.setId(BEDRIJF_ID);
		workFlowProcessObject.setActivityName(ACTIVITY_NAME);
		workFlowProcessObject.setAdminDomainName(BEDRIJF_NAME);
		workFlowProcessObject.setRequestedFor(REQUESTED_FOR);
		workFlowProcessObject.setRequestedBy(REQUESTED_BY);
		workFlowProcessObject.setRequestDate(REQUESTED_DATE);

		return workFlowProcessObject;
	}
}
