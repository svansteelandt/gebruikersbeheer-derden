package be.vdab.gebruikersbeheer.derden.intern.validator;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainSearch;
import be.vdab.gebruikersbeheer.derden.domain.PersonSearch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@Slf4j
public class SearchFormValidator implements Validator {

	private static final int DEFAULT_MAX_LENGTH = 150;

	private static final String ERROR_MAX_LENGTH= "error.maxlength";

	private static final String ERROR_TYPE_GETAL = "error.type.getal";
	
	public boolean supports(Class<?> clazz) {
		return false;
	}

	/**
	 * Validates person form&#46; <br>
	 * 
	 * @param object
	 *            object that needs to be validated
	 * @param errors
	 *            errors object that holds the error messages
	 */
	@Override
	public void validate(Object object, Errors errors) {
		log.debug("searchFormValidator.java - validate");

		if (object instanceof AdminDomainSearch adminDomainSearch) {

			if (!adminDomainSearch.hasSearchCriteria()) {
				errors.reject("criteria.required");
			}

			validateName(errors, adminDomainSearch.getName());
			if (inputIsTooLong(adminDomainSearch.getCity())) {
				errors.rejectValue("adminDomainSearch.city", ERROR_MAX_LENGTH, new Object[]{"gemeente", DEFAULT_MAX_LENGTH}, null);
			}
			if (inputIsTooLong(adminDomainSearch.getStreet())) {
				errors.rejectValue("adminDomainSearch.street", ERROR_MAX_LENGTH, new Object[]{"straat", DEFAULT_MAX_LENGTH}, null);
			}
			if (inputIsTooLong(adminDomainSearch.getPostalcode())) {
				errors.rejectValue("adminDomainSearch.postalcode", ERROR_MAX_LENGTH, new Object[]{"postcode", DEFAULT_MAX_LENGTH}, null);
			}
			if (inputIsTooLong(adminDomainSearch.getIkp()) || inputIsTooLong(adminDomainSearch.getIkpEnd())) {
				errors.rejectValue("adminDomainSearch.ikp", ERROR_MAX_LENGTH, new Object[]{"ikp", DEFAULT_MAX_LENGTH}, null);
			}

			validateOE(errors, "adminDomainSearch.oe", adminDomainSearch.getOe());
		}else if (object instanceof PersonSearch personSearch){

			if (!personSearch.hasSearchCriteria()) {
				errors.reject("criteria.required");
			}

			validateOE(errors, "personSearch.oe", personSearch.getOe());
		}
	}

	private void validateOE(Errors errors, String fieldName, String oe) {
		if (inputIsTooLong(oe, 8)) {
			errors.rejectValue(fieldName, ERROR_MAX_LENGTH, new Object[]{"OE", 8}, null);
		}else if (!isInteger(oe)){
			errors.rejectValue(fieldName, ERROR_TYPE_GETAL, new Object[]{"OE"}, null);
		}
	}

	private void validateName(Errors errors, String name) {
		if (StringUtils.isNotBlank(name)) {
			if (inputIsTooLong(name)) {
				errors.rejectValue("adminDomainSearch.name", ERROR_MAX_LENGTH, new Object[]{"naam", DEFAULT_MAX_LENGTH}, null);
			}else if (inputIsTooShort(name, 2)) {
				errors.rejectValue("adminDomainSearch.name", "error.minlength", new Object[]{"naam", 2}, null);
			}
		}
	}

	public static boolean inputIsTooLong(String input) {
		return inputIsTooLong(input, DEFAULT_MAX_LENGTH);
	}

	public static boolean inputIsTooLong(String input, int maxLength) {
		if (StringUtils.isEmpty(input)) return false;

		return input.length() > maxLength;
	}

	public static boolean inputIsTooShort(String input, int minLength) {
		if (StringUtils.isEmpty(input)) return false;

		return input.replace("*", "").length() < minLength;
	}

	public static boolean isInteger(String input){
		if (StringUtils.isEmpty(input)) return true;

		try {
			Integer.parseInt(input);

			return true;
		}catch(NumberFormatException f){
			return false;
		}
	}
}