package be.vdab.gebruikersbeheer.derden.util;

public interface GebruikersnaamUtils {
	static String removeSuffix(String username) {
		int index;

		username = username.toLowerCase();

		index = username.indexOf(",intern");
		if (index != -1) {
			return username.substring(0, index);
		}

		index = username.indexOf(",organisatie");
		if (index != -1) {
			return username.substring(0, index);
		}

		return username;
	}
}
