package be.vdab.gebruikersbeheer.derden.web.internal.v1;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class InszValidator implements ConstraintValidator<InszConstraint, String> {
    private static final int LENGTH = 11;
    private static final Pattern pattern = Pattern.compile("[0-9]{" + LENGTH + "}");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return matchesPattern(value) && hasValidCheckDigit(value);
    }

    private boolean matchesPattern(String value) {
        return value != null && value.length() == LENGTH && pattern.matcher(value).matches();
    }

    private boolean hasValidCheckDigit(String value) {
        String firstPart = value.substring(0, value.length() - 2);
        String checkDigitPart = value.substring(value.length() - 2);

        long providedFirstPart = Long.parseLong(firstPart);
        long providedCheckDigit = Long.parseLong(checkDigitPart);

        boolean correctPre2000 = checkDigitIsCorrect(providedFirstPart, providedCheckDigit);
        boolean correctPost2000 = checkDigitIsCorrect(providedFirstPart + 2_000_000_000, providedCheckDigit);

        return correctPre2000 || correctPost2000;
    }

    private boolean checkDigitIsCorrect(long firstPart, long checkDigits) {
        long rest = firstPart % 97;
        return checkDigits == 97 - rest;
    }
}
