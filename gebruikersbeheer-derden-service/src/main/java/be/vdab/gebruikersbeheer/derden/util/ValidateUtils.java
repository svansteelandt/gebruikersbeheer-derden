package be.vdab.gebruikersbeheer.derden.util;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.iam.validations.insz.InszValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateUtils {

	private final ApplicationProperties applicationProperties;

	public boolean validateEmailAddress(String strEmailAddress) {
		log.debug("validateEmailAddress {}", LogSanitizer.sanitize(strEmailAddress));

		return EmailValidator.getInstance().isValid(strEmailAddress);
	}

	public static boolean isValidInsz(String nationalNumber) {
		if (log.isDebugEnabled()) {
			log.debug("validateNationalNumber {}", LogSanitizer.sanitize(nationalNumber));
		}

		InszValidator inszValidator = new InszValidator();
		return inszValidator.isValid(nationalNumber);
	}

	public boolean isCVSRole(RoleObject roleObject) {
		return RoleNames.ROL_CVS.equalsIgnoreCase(roleObject.getRoleName());
	}

	public int maxAantalAdmins() {
		return this.applicationProperties.getMaxDomainAdmins();
	}

	public boolean maxAdminsReached(int aantalHuidigeAdmins) {
		return aantalHuidigeAdmins >= maxAantalAdmins();
	}

	public int minAantalAdmins() {
		return this.applicationProperties.getMinDomainAdmins();
	}

	public boolean minAdminsReached(int aantalHuidigeAdmins) {
		return aantalHuidigeAdmins <= minAantalAdmins();
	}

	public static String leadingZero(int getal) {
		if (getal < 10) {
			return "0" + getal;
		}

		return Integer.toString(getal);
	}
}
