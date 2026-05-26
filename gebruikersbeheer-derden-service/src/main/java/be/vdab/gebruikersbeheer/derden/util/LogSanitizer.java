package be.vdab.gebruikersbeheer.derden.util;

/**
 * Existence of this class is a smell. All inputs should be sanitized at the border (ie controller) of the system.
 */
public interface LogSanitizer {

	static String sanitize(String unsanitizedValue) {
		if (unsanitizedValue == null) {
			return null;
		}
		return unsanitizedValue.replaceAll("[\n\r\t]", "_");
	}
}
