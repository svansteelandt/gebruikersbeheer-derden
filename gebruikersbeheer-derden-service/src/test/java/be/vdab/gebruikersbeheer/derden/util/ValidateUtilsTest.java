package be.vdab.gebruikersbeheer.derden.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ValidateUtilsTest {

	@ParameterizedTest
	@ValueSource(strings = {
			"90122718707",
			"14121252705"
	})
	void validateNationalNumber(String insz) {
		assertThat(ValidateUtils.isValidInsz(insz)).isTrue();
	}
}