package be.vdab.gebruikersbeheer.derden.extern.validator;


import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.extern.view.PersonCommand;
import be.vdab.gebruikersbeheer.derden.util.RijksregisternummerGenerator;
import be.vdab.gebruikersbeheer.derden.util.ValidateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonFormValidatorTest {

	@Mock
	ValidateUtils validateUtils;

	@Mock
	RijksregisternummerGenerator rijksregisternummerGenerator;

	@InjectMocks
	PersonFormValidator personFormValidator;

	@Test
	@DisplayName("WHEN creating a new derde profile " +
			"AND all fields are filled out correctly " +
			"THEN no validation errors are found")
	void validateWithMobile() {
		when(validateUtils.validateEmailAddress(any())).thenReturn(true);
		PersonObject derdeToCreate = initDerde();
		var bindingResult = new BeanPropertyBindingResult(new PersonCommand(derdeToCreate), "personCommand");

		personFormValidator.validate(derdeToCreate, bindingResult);

		assertThat(bindingResult.hasErrors()).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"+355672623457", "+447853732825", "+31618307633"})
	void validateWithMobileAbroad(String mobile) {
		when(validateUtils.validateEmailAddress(any())).thenReturn(true);
		PersonObject derdeToCreate = initDerde();
		derdeToCreate.setMobile(mobile);
		var bindingResult = new BeanPropertyBindingResult(new PersonCommand(derdeToCreate), "personCommand");

		personFormValidator.validate(derdeToCreate, bindingResult);

		assertThat(bindingResult.hasErrors()).isFalse();
	}

	@Test
	@DisplayName("WHEN creating a new derde profile " +
			"THEN the mobile phone number is required")
	void validateWithoutMobile() {
		when(validateUtils.validateEmailAddress(any())).thenReturn(true);
		PersonObject derdeToCreate = initDerde();
		derdeToCreate.setMobile(null);
		var bindingResult = new BeanPropertyBindingResult(new PersonCommand(derdeToCreate), "personCommand");

		personFormValidator.validate(derdeToCreate, bindingResult);

		assertThat(bindingResult.hasErrors()).isTrue();
		assertThat(bindingResult.getErrorCount()).isEqualTo(1);
		assertThat(bindingResult.getFieldError("person.mobile").getCode()).isEqualTo("error.required");
	}

	@Test
	@DisplayName("WHEN creating a new derde profile WHERE an non E.164 compliant mobile phone number is entered, " +
			"THEN errors are returned")
	void mobileNotE164Compliant() {
		when(validateUtils.validateEmailAddress(any())).thenReturn(true);
		PersonObject derdeToCreate = initDerde();
		derdeToCreate.setPhone("+3252121212");
		derdeToCreate.setMobile("+3252121212");
		var bindingResult = new BeanPropertyBindingResult(new PersonCommand(derdeToCreate), "personCommand");

		personFormValidator.validate(derdeToCreate, bindingResult);

		assertThat(bindingResult.hasErrors()).isTrue();
		assertThat(bindingResult.getErrorCount()).isEqualTo(1);
		assertThat(bindingResult.getFieldError("person.mobile").getDefaultMessage()).isEqualTo("Dit is geen geldig gsm-nummer.");
	}

	@Test
	@DisplayName("""
			WHEN creating a new derde profile WHERE an non E.164 compliant phone number is entered,
			THEN errors are returned
			""")
	void phoneNotE164Compliant() {
		when(validateUtils.validateEmailAddress(any())).thenReturn(true);
		PersonObject derdeToCreate = initDerde();
		derdeToCreate.setPhone("123456");

		var bindingResult = new BeanPropertyBindingResult(new PersonCommand(derdeToCreate), "personCommand");

		personFormValidator.validate(derdeToCreate, bindingResult);

		assertThat(bindingResult.hasErrors()).isTrue();
		assertThat(bindingResult.getErrorCount()).isEqualTo(1);
		assertThat(bindingResult.getFieldError("person.phone").getDefaultMessage()).isEqualTo("Dit is geen geldig telefoonnummer.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"04/56 -.44&22. 60", "+32456pp442260p", "+32456pp442260p", "+3245&6442260", "0456pp442260p"})
	@DisplayName("WHEN creating a new derde profile WHERE check mobile phone (remove bad characters)")
	void validate7(String mobilePhone) {
		when(validateUtils.validateEmailAddress(any())).thenReturn(true);
		PersonObject derdeToCreate = initDerde();
		derdeToCreate.setMobile(mobilePhone);
		var bindingResult = new BeanPropertyBindingResult(new PersonCommand(derdeToCreate), "personCommand");

		personFormValidator.validate(derdeToCreate, bindingResult);

		assertThat(bindingResult.hasFieldErrors()).isFalse();
	}

	@Test
	void whenRrnIsInvalid_thenValidationFails() {
		when(validateUtils.validateEmailAddress(any())).thenReturn(true);

		PersonObject derdeToCreate = initDerde();
		derdeToCreate.setNationalNumber("70121312372");

		var bindingResult = new BeanPropertyBindingResult(new PersonCommand(derdeToCreate), "personCommand");
		personFormValidator.validate(derdeToCreate, bindingResult);

		assertThat(bindingResult.getFieldErrors()).hasSize(1);
		FieldError rrnFieldError = bindingResult.getFieldError("person.nationalNumber");
		assertThat(rrnFieldError.getCode()).isEqualTo("error.validate");
		assertThat(rrnFieldError.getArguments()).containsExactly("Rijksregister");
	}

	@Test
	void validateEmailRequired() {
		PersonObject derdeToCreate = initDerde();
		derdeToCreate.setEmailAddress(null);
		var bindingResult = new BeanPropertyBindingResult(new PersonCommand(derdeToCreate), "personCommand");

		personFormValidator.validate(derdeToCreate, bindingResult);

		assertThat(bindingResult.hasFieldErrors()).isTrue();
		assertThat(bindingResult.getFieldError("person.emailAddress").getCode()).isEqualTo("error.required");

		Object[] arguments = Objects.requireNonNull(bindingResult.getFieldError("person.emailAddress")).getArguments();
		assertThat(arguments).isNotNull().hasSize(1);
		assertThat(arguments[0]).isEqualTo("Email");
	}

	@Test
	void validatePhoneNumberRequired() {
		PersonObject derdeToCreate = initDerde();
		derdeToCreate.setPhone(null);
		var bindingResult = new BeanPropertyBindingResult(new PersonCommand(derdeToCreate), "personCommand");

		personFormValidator.validate(derdeToCreate, bindingResult);

		assertThat(bindingResult.hasFieldErrors()).isTrue();
		assertThat(bindingResult.getFieldError("person.phone").getCode()).isEqualTo("error.required");

		Object[] arguments = Objects.requireNonNull(bindingResult.getFieldError("person.phone")).getArguments();
		assertThat(arguments).isNotNull().hasSize(1);
		assertThat(arguments[0]).isEqualTo("Telefoon");
	}

	@ParameterizedTest
	@ValueSource(strings = {"022503611", "+3222503611"})
	@DisplayName("""
			WHEN creating a new derde profile WITH correct phonenumbers (with and without landcode)
			THEN returns no errors
			""")
	void validatePhoneNumbers(String phone) {
		when(validateUtils.validateEmailAddress(any())).thenReturn(true);
		PersonObject derdeToCreate = initDerde();
		derdeToCreate.setPhone(phone);

		var bindingResult = new BeanPropertyBindingResult(new PersonCommand(derdeToCreate), "personCommand");
		personFormValidator.validate(derdeToCreate, bindingResult);

		assertThat(bindingResult.hasFieldErrors()).isFalse();
	}

	@Test
	@DisplayName("""
			WHEN derde user WITHOUT rrnr
			THEN rijksregisternummer is generated
			""")
	void validateRrn1() {
		when(rijksregisternummerGenerator.getNextRijksregisterNummer()).thenReturn(100000000L);
		when(validateUtils.validateEmailAddress(any())).thenReturn(true);

		PersonObject derdeToCreate = initDerde();
		derdeToCreate.setNoRrn(true);

		var bindingResult = new BeanPropertyBindingResult(new PersonCommand(derdeToCreate), "personCommand");
		personFormValidator.validate(derdeToCreate, bindingResult);

		assertThat(bindingResult.hasFieldErrors()).isFalse();
	}

	@Test
	@DisplayName("""
			WHEN incomplete rrn
			THEN returns bindingresult WITH error.validate FOR field nationalNumber
			""")
	void validateIncompleteRrnr() {
		when(validateUtils.validateEmailAddress(any())).thenReturn(true);
		PersonObject derdeToCreate = initDerde();
		derdeToCreate.setNoRrn(false);
		derdeToCreate.setNationalNumber("930133778");

		var bindingResult = new BeanPropertyBindingResult(new PersonCommand(derdeToCreate), "personCommand");
		personFormValidator.validate(derdeToCreate, bindingResult);

		assertThat(bindingResult.hasFieldErrors()).isTrue();
		assertThat(bindingResult.getFieldError("person.nationalNumber").getCode()).isEqualTo("error.validate");
	}

	private PersonObject initDerde() {
		PersonObject derdeToCreate = new PersonObject();
		derdeToCreate.setFirstName("Dries");
		derdeToCreate.setLastName("Thieren");
		derdeToCreate.setEmailAddress("jefke@vdab.be");
		derdeToCreate.setPhone("+32485446635");
		derdeToCreate.setMobile("+32485446635");
		derdeToCreate.setNationalNumber("70121312371");

		return derdeToCreate;
	}
}
