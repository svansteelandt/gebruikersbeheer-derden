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

import java.util.List;

import static be.vdab.gebruikersbeheer.derden.asserters.ConstraintViolationAssert.assertThat;

class CreateGebruikerCommandTest {

	private static Validator validator;

	public CreateGebruikerCommand validCreateGebruikerCommand;

	@BeforeAll
	static void setUp() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			validator = factory.getValidator();
		}
	}

	@BeforeEach
	void setup() {
		validCreateGebruikerCommand = CreateGebruikerCommand.builder()
				.email("john@doe.com")
				.firstName("John")
				.lastName("Doe")
				.rrnr("81030300377")
				.phone("+32485112233")
				.mobile("+32485112233")
				.roleGlobalIds(List.of("a-role"))
				.build();
	}

	@DisplayName("WHEN valid command THEN validation succeeds")
	@Test
	void whenValidCommand_thenValidationSucceeds() {
		assertThat(validator.validate(validCreateGebruikerCommand)).hasNoViolations();
	}


	@DisplayName("WHEN phone number is not formatted as E164 THEN validation fails")
	@Test
	void whenPhoneIsNotE164_thenValidationFails() {
		var createGebruikerWithInvalidPhone = validCreateGebruikerCommand.toBuilder()
				.phone("03712112233")
				.build();
		assertThat(validator.validate(createGebruikerWithInvalidPhone))
				.hasViolationForProperty("phone", E164.class);
	}

	@DisplayName("WHEN mobile number is not formatted as E164 THEN validation fails")
	@Test
	void whenMobileIsNotE164_thenValidationFails() {
		var createGebruikerWithInvalidMobileNumber = validCreateGebruikerCommand.toBuilder()
				.mobile("0485112233")
				.build();
		assertThat(validator.validate(createGebruikerWithInvalidMobileNumber))
				.hasViolationForProperty("mobile", E164.class);
	}

	@DisplayName("WHEN mobile number contains a fixed line THEN validation fails")
	@Test
	void whenMobileNumberIsFixedLine_thenValidationFails() {
		String fixedLine = "+32777665544";
		var createGebruikerWithInvalidMobileNumber = validCreateGebruikerCommand.toBuilder()
				.mobile(fixedLine)
				.build();
		assertThat(validator.validate(createGebruikerWithInvalidMobileNumber))
				.hasViolationForProperty("mobile", Gsm.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {"+32475424623", "+13255551234", "+355672623457"})
	void internationalPhoneNumbersAreAccepted(String phoneNumber) {
		var validCreateGebruikerCommand = this.validCreateGebruikerCommand.toBuilder()
				.phone(phoneNumber)
				.mobile(phoneNumber)
				.build();
		assertThat(validator.validate(validCreateGebruikerCommand))
				.hasNoViolations();
	}


}
