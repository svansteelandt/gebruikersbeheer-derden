package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonCreatorTest {

	@Mock
	IsimWsSession isimWsSession;

	@Mock
	IsimWsClient isimWsClient;

	@Mock
	IsimLdapManager isimLdapManager;
	
	@Mock
	IsimUserContextManager isimUserContextManager;

	@InjectMocks
	PersonCreator personCreator;

	@Test
	void createPersonInIsim1() {
		when(isimLdapManager.getPersonDnByRrnAndAdminDomainDn(any(), any())).thenReturn(Optional.of(new Dn("dn")));
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);

		var result = personCreator.createPersonInIsim(new AdminDomainObject(new Dn("value")), new PersonObject());

		assertThat(result).isNotEmpty();
	}
}