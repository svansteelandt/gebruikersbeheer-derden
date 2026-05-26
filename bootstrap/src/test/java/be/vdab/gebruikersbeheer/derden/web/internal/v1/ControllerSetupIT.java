package be.vdab.gebruikersbeheer.derden.web.internal.v1;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.iam.oidc.WithJwtPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ControllerSetupIT extends BaseIT {

	@Test
	@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void stringFormatting() throws Exception {
		initIsimUser();

		mockMvc.perform(get("/api/internal/test").param("test", "     Jef   Patat   "))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("Jef   Patat"));
	}
}

@Controller
@RequestMapping(value = "/api/internal/test")
class TestController {
	@GetMapping
	public String test(String test) {
		return test;
	}
}