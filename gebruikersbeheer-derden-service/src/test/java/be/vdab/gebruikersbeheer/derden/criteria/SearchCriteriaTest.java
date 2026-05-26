package be.vdab.gebruikersbeheer.derden.criteria;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SearchCriteriaTest {
	@Test
	void toStringTest() {
		SearchCriteria searchCriteria = new SearchCriteria();
		Assertions.assertThat(searchCriteria.toString()).isNotNull();
	}
}
