package be.vdab.gebruikersbeheer.derden.core.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CodeObjectTest {

	@Test
	@DisplayName("""
			WHEN code WITH kort label
			THEN getLabel return kort label
			""")
	void getKortLabelWhenKortLabelExists() {
		Code codeObject = new Code();
		codeObject.setWaarde("w");
		codeObject.setKortLabel("kort");

		Assertions.assertThat(codeObject.getLabel()).isEqualTo(codeObject.getKortLabel());
	}

	@Test
	@DisplayName("""
			WHEN code WITHOUT kort label
			THEN getLabel return waarde
			""")
	void getWaardeWhenKortLabelNotExists() {
		Code codeObject = new Code();
		codeObject.setWaarde("w");
		codeObject.setKortLabel("");

		Assertions.assertThat(codeObject.getLabel()).isEqualTo(codeObject.getWaarde());
	}
}
