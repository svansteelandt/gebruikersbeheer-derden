package be.vdab.gebruikersbeheer.derden.web.admin;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSystemUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GebruikersbeheerCacheControllerIT extends BaseIT {

	@BeforeEach
	void beforeEach() {
		String gebruikersnaam = "JMEYS";

		PersonObject personObject = initIsimUser(gebruikersnaam);

		IsimWsSession isimSession = mock(IsimWsSession.class);
		IsimWsSystemUser systemUser = mock(IsimWsSystemUser.class);
		when(systemUser.getUid()).thenReturn(gebruikersnaam);
		when(systemUser.getDn()).thenReturn(new Dn("erglobalid=123,ou=0,ou=psersons"));
		when(systemUser.getOwner()).thenReturn(personObject.getDn());
		when(isimUserContextManager.getSession()).thenReturn(isimSession);
		when(isimWsClient.getSystemUser(any(IsimWsSession.class), anyString())).thenReturn(Optional.of(systemUser));
	}

	@Test
	void clearCache() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/gebruikersbeheer/v1/cache/clear")
						.header("vdabauthorization", "cn=JMEYS,ou=users,ou=intern,O=VDAB"))
				.andExpect(status().isOk());
	}

	@Test
	void clearCacheForUsername() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/gebruikersbeheer/v1/cache/jos/clear")
						.header("vdabauthorization", "cn=JMEYS,ou=users,ou=intern,O=VDAB"))
				.andExpect(status().isOk());
	}

	@Test
	void clearCacheForOrganisatie() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/gebruikersbeheer/v1/cache/clear?ikp=203000")
						.header("vdabauthorization", "cn=JMEYS,ou=users,ou=intern,O=VDAB"))
				.andExpect(status().isOk());
	}
}