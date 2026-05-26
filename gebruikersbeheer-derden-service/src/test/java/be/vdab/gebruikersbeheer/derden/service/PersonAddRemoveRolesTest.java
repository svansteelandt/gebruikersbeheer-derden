package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.converter.PersonConverter;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import be.vdab.gebruikersbeheer.util.isim.service.IsimRoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonAddRemoveRolesTest {

	@Mock
	PersonConverter personConverter;

	@Mock
	IsimLdapManager isimLdapManager;

	@Mock
	RoleService roleService;

	@Mock
	IsimWsSession isimWsSession;

	@Mock
	IsimRoleService isimRoleService;

	@Mock
	AdminDomainService adminDomainService;
	@Mock
	IsimUserContextManager isimUserContextManager;

	@InjectMocks
	PersonServiceImpl personService;

	@Test
	void toevoegenRol() {
		IsimLdapPerson person = mock(IsimLdapPerson.class);
		RoleObject role = mock(RoleObject.class);

		when(isimLdapManager.getPersonByUid(anyString(), anyList())).thenReturn(Optional.of(person));
		when(personConverter.convert(person, true)).thenReturn(new PersonObject());
		when(roleService.findByRoleName(anyString())).thenReturn(Optional.of(role));
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);


		this.personService.toevoegenRol("Test", "rol");

		assertThat(person).isNotNull();
	}

	@Test
	void toevoegenReedsToegekendeRol() {
		IsimLdapPerson person = mock(IsimLdapPerson.class);
		PersonObject personObject = mock(PersonObject.class);
		RoleObject role = mock(RoleObject.class);

		when(isimLdapManager.getPersonByUid(anyString(), anyList())).thenReturn(Optional.of(person));
		when(personConverter.convert(person, true)).thenReturn(personObject);
		when(roleService.findByRoleName(anyString())).thenReturn(Optional.of(role));
		when(role.getRoleName()).thenReturn("role");
		when(personObject.getRoles()).thenReturn(Collections.singletonList(role));

		this.personService.toevoegenRol("Test", "rol");

		assertThat(person).isNotNull();
	}

	@Test
	void verwijderenRol() {
		IsimLdapPerson person = mock(IsimLdapPerson.class);
		PersonObject personObject = mock(PersonObject.class);
		RoleObject role = mock(RoleObject.class);

		when(isimLdapManager.getPersonByUid(anyString(), anyList())).thenReturn(Optional.of(person));
		when(personConverter.convert(person, true)).thenReturn(personObject);
		when(roleService.findByRoleName(anyString())).thenReturn(Optional.of(role));
		when(role.getRoleName()).thenReturn("role");
		when(personObject.getRoles()).thenReturn(Collections.singletonList(role));
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);

		this.personService.verwijderenRol("Test", role.getRoleName());
		assertThat(person).isNotNull();
	}

	@Test
	void verwijderenNietToegekendeRol() {
		IsimLdapPerson person = mock(IsimLdapPerson.class);
		PersonObject personObject = mock(PersonObject.class);
		RoleObject role = mock(RoleObject.class);

		when(isimLdapManager.getPersonByUid(anyString(), anyList())).thenReturn(Optional.of(person));
		when(personConverter.convert(person, true)).thenReturn(personObject);
		when(roleService.findByRoleName(anyString())).thenReturn(Optional.of(role));
		when(role.getRoleName()).thenReturn("role");
		when(personObject.getRoles()).thenReturn(Collections.emptyList());

		this.personService.verwijderenRol("Test", role.getRoleName());
		assertThat(person).isNotNull();
	}
}
