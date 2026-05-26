package be.vdab.gebruikersbeheer.derden.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class LogSanitizerTest {


	@ParameterizedTest
	@CsvSource({"hal\rlo, hal_lo",
			"hallo, hallo",
			",",
			"foo\tbar, foo_bar",
			"foo\tbar\rbar, foo_bar_bar"
	})
	void sanitizesCorrectly(String input, String output) {
		assertThat(LogSanitizer.sanitize(input)).isEqualTo(output);
	}

	@Test
	void sanitizesReturnCorrectly() {
		assertThat(LogSanitizer.sanitize("blub\nblub")).isEqualTo("blub_blub");
		assertThat(LogSanitizer.sanitize("blub\nblub\tblub\r")).isEqualTo("blub_blub_blub_");
	}

}