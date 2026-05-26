package be.vdab.gebruikersbeheer.derden.validation;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class EmailValidatorIT extends BaseIT {

	@Test
	void validEmailAdres() {
		Assertions.assertThat(validateUtils.validateEmailAddress("kris.jespers@test.com")).isTrue();
	}

	@Test
	void inValidEmailAdres() {
		Assertions.assertThat(validateUtils.validateEmailAddress("kris.jesperstest.com")).isFalse();
	}

	@Test
	void inValidEmailAdres2() {
		Assertions.assertThat(validateUtils.validateEmailAddress("kris.jespers@gmail.comm")).isFalse();
	}
}
