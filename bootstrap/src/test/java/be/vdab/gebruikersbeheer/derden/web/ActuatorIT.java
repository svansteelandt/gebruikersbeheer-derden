package be.vdab.gebruikersbeheer.derden.web;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActuatorIT extends BaseIT {
	@Test
	@DisplayName("WHEN /info is called" +
			 " THEN response should contain application properties")
	void test() throws Exception {
		mockMvc.perform(get("/gebruikersbeheer-derden/info").contextPath("/gebruikersbeheer-derden"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("applicationProperties").exists())
				.andExpect(jsonPath("applicationProperties").isNotEmpty());
	}
}
