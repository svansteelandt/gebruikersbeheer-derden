package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands;

import be.vdab.iam.validations.phone.annotation.E164;
import be.vdab.validatie.constraint.gsm.Gsm;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static be.vdab.gebruikersbeheer.derden.asserters.ConstraintViolationAssert.assertThat;

class EditGebruikerCommandTest {

	private static Validator validator;

	public EditGebruikerCommand validEditGebruikerCommand;

	@BeforeAll
	static void setUp() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			validator = factory.getValidator();
		}
	}

	@BeforeEach
	void setup() {
		validEditGebruikerCommand = EditGebruikerCommand.builder()
				.email("john@doe.com")
				.firstName("John")
				.lastName("Doe")
				.phone("+32485112233")
				.mobile("+32485112233")
				.suspend(false)
				.build();
	}

	@Test
	void whenValidCommand_thenValidationSucceeds() {
		assertThat(validator.validate(validEditGebruikerCommand)).hasNoViolations();
	}


	@DisplayName("WHEN phone is not formatted as E164 THEN validation fails")
	@Test
	void whenPhoneIsNotE164_thenValidationFails() {
		var editGebruikerWithInvalidPhone = validEditGebruikerCommand.toBuilder()
				.phone("03712112233")
				.build();
		assertThat(validator.validate(editGebruikerWithInvalidPhone))
				.hasViolationForProperty("phone", E164.class);
	}


	@DisplayName("WHEN mobile is not formatted as E164 THEN validation fails")
	@Test
	void whenMobileIsNotE164_thenValidationFails() {
		var editGebruikerWithInvalidMobileNumber = validEditGebruikerCommand.toBuilder()
				.mobile("0485112233")
				.build();
		assertThat(validator.validate(editGebruikerWithInvalidMobileNumber))
				.hasViolationForProperty("mobile", E164.class);
	}

	@DisplayName("WHEN mobile number contains a fixed line THEN validation fails")
	@Test
	void whenMobileNumberIsFixedLine_thenValidationFails() {
		String fixedLine = "+32777665544";
		var editGebruikerWithInvalidMobileNumber = validEditGebruikerCommand.toBuilder()
				.mobile(fixedLine)
				.build();
		assertThat(validator.validate(editGebruikerWithInvalidMobileNumber))
				.hasViolationForProperty("mobile", Gsm.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {"+33612345678", "+13255551234", "+355672623457"})
	void internationalPhoneNumbersAreAccepted(String phoneNumber) {
		var valideditGebruikerCommand = this.validEditGebruikerCommand.toBuilder()
				.phone(phoneNumber)
				.mobile(phoneNumber)
				.build();
		assertThat(validator.validate(valideditGebruikerCommand))
				.hasNoViolations();
	}


}
