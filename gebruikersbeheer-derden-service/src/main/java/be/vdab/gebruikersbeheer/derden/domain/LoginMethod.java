package be.vdab.gebruikersbeheer.derden.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

public enum LoginMethod {

	NONE("0"), UP("1"), OTP("5"), ACM("10");

	public static Comparator<LoginMethod> COMPARATOR = Comparator.comparing(LoginMethod::getValue);

	private final String value;

	LoginMethod(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static LoginMethod fromValue(String value) {
		if (StringUtils.isEmpty(value)) {
			return NONE;
		}

		for (LoginMethod loginMethod : values()) {
			if (loginMethod.value.equals(value)) {
				return loginMethod;
			}
		}

		throw new IllegalArgumentException("Unknown loginmethod " + value);
	}
}
