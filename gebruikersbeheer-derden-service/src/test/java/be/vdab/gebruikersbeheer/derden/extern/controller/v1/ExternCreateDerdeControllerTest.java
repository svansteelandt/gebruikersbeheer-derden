package be.vdab.gebruikersbeheer.derden.extern.controller.v1;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.extern.service.ExternPersonService;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternCreateDerdeControllerTest {

	public static final String NEW_EMAIL_ADDRESS = "joske@vdab.be";
	public static final String NEW_MOBILE_NUMBER = "0478731201";
	public static final String ORGANISATION_NAME = "Joske BV";
	public static final String ERROR_MESSAGE = "iets";
	public static final String CREATE_ERROR_MESSAGE = "kapot";
	public static final String CREATE_ERROR_KEY = "errorCreate";
	String VALID_TOKEN = "validToken";
	String INVALID_TOKEN = "invalidToken";

	@Mock
	ExternPersonService externPersonService;
	@Mock
	ApplicationProperties applicationProperties;
	@Mock
	Model model;

	@InjectMocks
	ExternCreateDerdeController controller;

	@BeforeEach
	void beforeEach() {
		ExternPersonService.DerdeData derdeData = ExternPersonService.DerdeData.builder()
				.newEmailAdress(NEW_EMAIL_ADDRESS)
				.newMobileNumber(NEW_MOBILE_NUMBER)
				.organisationName(ORGANISATION_NAME)
				.build();
		configureExternPersonServiceMock(derdeData);
	}

	@Test
	@DisplayName("When a valid token is presented " +
			"then the confirmation page is shown")
	void getCreateDerdeViaToken() {
		when(applicationProperties.getWerkgeversUrl()).thenReturn("https://werkgevers-rel1ldv.ops.vdab.be");
		String result = controller.getCreateDerdeViaToken(VALID_TOKEN, model);

		assertThat(result).isEqualTo("/extern/person/create/confirmation");
		verify(model).addAttribute("newMobileNumber", NEW_MOBILE_NUMBER);
		verify(model).addAttribute("newEmailAddress", NEW_EMAIL_ADDRESS);
		verify(model).addAttribute("organisationName", ORGANISATION_NAME);
		verify(model).addAttribute("cancelLink", "https://werkgevers-rel1ldv.ops.vdab.be");
	}

	@Test
	@DisplayName("When an invalid token is presented " +
			"then the failure page is shown")
	void getCreateDerdeViaToken2() {
		String result = controller.getCreateDerdeViaToken(INVALID_TOKEN, model);

		assertThat(result).isEqualTo("/extern/person/create/failure");
		verify(model).addAttribute("errorMessage", ERROR_MESSAGE);
	}

	@Test
	@DisplayName("When a valid token is posted to create a derde" +
			"then the success page is shown")
	void postCreateDerdeViaToken() {
		String result = controller.postCreateDerdeViaToken(tokenForm(VALID_TOKEN), model);

		assertThat(result).isEqualTo("/extern/person/create/success");
	}

	@Test
	@DisplayName("When an invalid token is posted to create a derde" +
			"then the failure page is shown")
	void postCreateDerdeViaToken2() {
		String result = controller.postCreateDerdeViaToken(tokenForm(INVALID_TOKEN), model);

		assertThat(result).isEqualTo("/extern/person/create/failure");
		verify(model).addAttribute("errorMessage", CREATE_ERROR_MESSAGE);
	}

	@NotNull
	private ExternCreateDerdeController.TokenForm tokenForm(String token) {
		ExternCreateDerdeController.TokenForm tokenForm = new ExternCreateDerdeController.TokenForm();
		tokenForm.setToken(token);
		return tokenForm;
	}

	private void configureExternPersonServiceMock(ExternPersonService.DerdeData derdeData) {
		ExternPersonService.CanCreateDerdeResult canCreateDerdeResult = ExternPersonService.CanCreateDerdeResult.success(derdeData);
		lenient().when(externPersonService.canCreateDerdeFromToken(VALID_TOKEN)).thenReturn(canCreateDerdeResult);

		ExternPersonService.CreatePersonResult createPersonResult = ExternPersonService.CreatePersonResult.success("successKey", "successMessage");
		lenient().when(externPersonService.createDerdeFromToken(VALID_TOKEN)).thenReturn(createPersonResult);

		ExternPersonService.CanCreateDerdeResult canCreateInvalidPersonResult = ExternPersonService.CanCreateDerdeResult.failure(ERROR_MESSAGE);
		lenient().when(externPersonService.canCreateDerdeFromToken(INVALID_TOKEN)).thenReturn(canCreateInvalidPersonResult);

		ExternPersonService.CreatePersonResult createInvalidPersonResult = ExternPersonService.CreatePersonResult.failure(CREATE_ERROR_KEY, CREATE_ERROR_MESSAGE);
		lenient().when(externPersonService.createDerdeFromToken(INVALID_TOKEN)).thenReturn(createInvalidPersonResult);
	}
}