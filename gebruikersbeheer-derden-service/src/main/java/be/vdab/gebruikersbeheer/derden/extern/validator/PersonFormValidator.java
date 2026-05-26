package be.vdab.gebruikersbeheer.derden.extern.validator;

import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.util.RijksregisternummerGenerator;
import be.vdab.gebruikersbeheer.derden.util.ValidateUtils;
import be.vdab.iam.validations.phone.VdabPhoneNumberUtil;
import be.vdab.validatie.core.gsm.GsmValidator;
import be.vdab.validatie.core.telefoon.TelefoonValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class PersonFormValidator implements Validator {

	private static final String ERROR_REQUIRED = "error.required";
	private final ValidateUtils validateUtils;
	private final RijksregisternummerGenerator rijksregisternummerGenerator;

	@Override
	public boolean supports(Class<?> clazz) {
		return false;
	}

	@Override
	public void validate(Object object, Errors errors) {
		PersonObject personObject = (PersonObject) object;
		if (personObject == null) return;

		validateFirstname(errors, personObject);

		validateLastname(errors, personObject);

		validatePhone(errors, personObject);

		validateMobile(errors, personObject);

		validateEmail(errors, personObject);

		validateRRN(errors, personObject);
	}

	private void validateFirstname(Errors errors, PersonObject personObject) {
		if (personObject.getFirstName() != null && (personObject.getFirstName().contains("(") || personObject.getFirstName().contains(")"))) {
			errors.rejectValue("person.firstName", "", new Object[]{"Voornaam"}, "Volgende karakters zijn niet toegestaan : ( en )");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "person.firstName", ERROR_REQUIRED, new Object[]{"Voornaam"});
		}
	}

	private void validateLastname(Errors errors, PersonObject personObject) {
		if (personObject.getLastName() != null && (personObject.getLastName().contains("(") || personObject.getLastName().contains(")"))) {
			errors.rejectValue("person.lastName", "", new Object[]{"Familienaam"}, "Volgende karakters zijn niet toegestaan : ( en )");
		} else {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "person.lastName", ERROR_REQUIRED, new Object[]{"Familienaam"});
		}
	}

	private void validatePhone(Errors errors, PersonObject personObject) {
		personObject.setPhone(cleanPhoneNumber(personObject.getPhone()));

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "person.phone", ERROR_REQUIRED, new Object[]{"Telefoon"});

		if (phoneIsNonE164Compliant(personObject.getPhone())) {
			errors.rejectValue("person.phone", "", new Object[]{"Telefoon"}, "Dit is geen geldig telefoonnummer.");
		} else {
			personObject.setPhone(formatToE164(personObject.getPhone()));
		}
	}

	private void validateRRN(Errors errors, PersonObject personObject) {
		if (personObject.isNoRrn()) {
			personObject.setNationalNumber(Long.toString(this.rijksregisternummerGenerator.getNextRijksregisterNummer()));
		} else if (!ValidateUtils.isValidInsz(personObject.getNationalNumber())) {
			errors.rejectValue("person.nationalNumber", "error.validate", new Object[]{"Rijksregister"}, null);
		}
	}

	private void validateEmail(Errors errors, PersonObject personObject) {
		// email
		if (StringUtils.isEmpty(personObject.getEmailAddress())) {
			errors.rejectValue("person.emailAddress", ERROR_REQUIRED, new Object[]{"Email"}, null);
		} else if (!this.validateUtils.validateEmailAddress(personObject.getEmailAddress())) {
			errors.rejectValue("person.emailAddress", "", new Object[]{"Email"}, "Dit is een ongeldig mailadres");
		}
	}

	private void validateMobile(Errors errors, PersonObject personObject) {
		personObject.setMobile(cleanPhoneNumber(personObject.getMobile()));

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "person.mobile", ERROR_REQUIRED, new Object[]{"Gsm"});

		if (mobileIsNonE164Compliant(personObject.getMobile())) {
			errors.rejectValue("person.mobile", "", new Object[]{"Gsm"}, "Dit is geen geldig gsm-nummer.");
		} else {
			personObject.setMobile(formatToE164(personObject.getMobile()));
		}
	}

	private boolean mobileIsNonE164Compliant(String mobile) {
		return StringUtils.isNotEmpty(mobile) && !GsmValidator.isGsmValid(mobile);
	}

	private boolean phoneIsNonE164Compliant(String phone) {
		return StringUtils.isNotEmpty(phone) &&
				!(TelefoonValidator.isTelefoonValid(phone) || TelefoonValidator.isTelefoonValid(phone, "BE"));
	}

	private String cleanPhoneNumber(String phoneNumber) {
		if (StringUtils.isEmpty(phoneNumber)) return phoneNumber;

		String prefix = phoneNumber.startsWith("+") ? "+" : "";

		// remove all not digits
		return "%s%s".formatted(prefix, phoneNumber.replaceAll("[\\D]", ""));
	}

	public String formatToE164(String mobile) {
		return VdabPhoneNumberUtil.formatToE164(mobile);
	}
}
