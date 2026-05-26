package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.converter.PersonConverter;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextHolder;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.ldap.LdapAbstraction;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import be.vdab.gebruikersbeheer.util.isim.ldap.LdapAttributesBuilder;
import be.vdab.gebruikersbeheer.util.service.BusinessWebservice;
import be.vdab.gebruikersbeheer.util.service.CreatePersonRequest;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsertPersonTest {

	@Mock
	BusinessWebservice businessWebservice;
	@Mock
	IsimWsSession isimWsSession;
	@Mock
	IsimLdapManager isimLdapManager;
	@Mock
	IsimWsClient isimWsClient;
	@Mock
	PersonCreator personCreator;
	@Mock
	IsimUserContextHolder isimContextHolder;

	@Spy
	PersonConverter personConverter = new PersonConverter(null, null, null);

	@InjectMocks
	PersonServiceImpl personService;


	@Test
	@DisplayName("WHEN a derde is created without other profiles, " +
			"THEN it is created with authenticationlevel 5")
	void insertPerson() {
		String globalId = "99874563";
		Dn dn = ObjectCreator.getDn();
		AdminDomainObject adminDomainObject = ObjectCreator.createAdminDomainObject(dn);
		PersonObject personObject = ObjectCreator.createPersonObject(globalId);
		when(businessWebservice.createPerson(any(CreatePersonRequest.class))).thenReturn(2L);

		when(isimLdapManager.getPersonsByFilter(any(), any())).thenReturn(List.of());
		when(personCreator.createPersonInIsim(any(), any())).thenReturn(Optional.of(dn));

		Dn createdDn = personService.insertPerson(adminDomainObject, personObject, false);
		assertThat(createdDn).isNotNull();
		assertThat(createdDn.getValue()).isEqualTo("erglobalid=692359561954987775,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");

		ArgumentCaptor<CreatePersonRequest> createPersonRequestArgumentCaptor = ArgumentCaptor.forClass(CreatePersonRequest.class);
		verify(businessWebservice).createPerson(createPersonRequestArgumentCaptor.capture());

		CreatePersonRequest createPersonRequest = createPersonRequestArgumentCaptor.getValue();
		assertThat(createPersonRequest.getAanspreektitel()).isNull();
		assertThat(createPersonRequest.getInsz()).isEqualTo(personObject.getNationalNumber());
		assertThat(createPersonRequest.getNaam()).isEqualTo(personObject.getLastName());
		assertThat(createPersonRequest.getVoornaam()).isEqualTo(personObject.getFirstName());
		assertThat(createPersonRequest.getEmail()).isEqualTo(personObject.getEmailAddress());

		ArgumentCaptor<PersonObject> personObjectArgumentCaptor = ArgumentCaptor.forClass(PersonObject.class);
		verify(personCreator).createPersonInIsim(any(), personObjectArgumentCaptor.capture());

		assertThat(personObjectArgumentCaptor.getValue().getLoginMethod()).isEqualTo("5");
	}

	@Test
	@DisplayName("WHEN a person is created and already has profiles with authenticationlevels 5 and 10, " +
			"THEN that new person will get authentication level 1")
	void insertPerson2() {
		String globalId = "99874563";
		Dn dn = ObjectCreator.getDn();
		AdminDomainObject adminDomainObject = ObjectCreator.createAdminDomainObject(dn);
		PersonObject personObject = ObjectCreator.createPersonObject(globalId);
		when(businessWebservice.createPerson(any(CreatePersonRequest.class))).thenReturn(2L);

		when(isimLdapManager.getPersonsByFilter(any(), any())).thenReturn(List.of(
				ldapPerson("dn1", "5"),
				ldapPerson("dn2", "1"),
				ldapPerson("dn3", "10")));
		when(personCreator.createPersonInIsim(any(), any())).thenReturn(Optional.of(dn));

		personService.insertPerson(adminDomainObject, personObject, false);

		ArgumentCaptor<PersonObject> personObjectArgumentCaptor = ArgumentCaptor.forClass(PersonObject.class);
		verify(personCreator).createPersonInIsim(any(), personObjectArgumentCaptor.capture());

		assertThat(personObjectArgumentCaptor.getValue().getLoginMethod()).isEqualTo("5");
	}

	@Test
	@DisplayName("WHEN a person is created and has profiles with only authenticationlevels 5 and 10, " +
			"THEN that new person will get authentication level 5")
	void insertPerson3() {
		String globalId = "99874563";
		Dn dn = ObjectCreator.getDn();
		AdminDomainObject adminDomainObject = ObjectCreator.createAdminDomainObject(dn);
		PersonObject personObject = ObjectCreator.createPersonObject(globalId);
		when(businessWebservice.createPerson(any(CreatePersonRequest.class))).thenReturn(2L);

		when(isimLdapManager.getPersonsByFilter(any(), any())).thenReturn(List.of(
				ldapPerson("dn1", "5"),
				ldapPerson("dn2", "5"),
				ldapPerson("dn3", "10")));
		when(personCreator.createPersonInIsim(any(), any())).thenReturn(Optional.of(dn));

		personService.insertPerson(adminDomainObject, personObject, false);

		ArgumentCaptor<PersonObject> personObjectArgumentCaptor = ArgumentCaptor.forClass(PersonObject.class);
		verify(personCreator).createPersonInIsim(any(), personObjectArgumentCaptor.capture());

		assertThat(personObjectArgumentCaptor.getValue().getLoginMethod()).isEqualTo("5");
	}

	@Test
	@DisplayName("WHEN a person is created and has profiles with only authenticationlevels 1, " +
			"THEN that new person will get authentication level 1")
	void insertPerson4() {
		String globalId = "99874563";
		Dn dn = ObjectCreator.getDn();
		AdminDomainObject adminDomainObject = ObjectCreator.createAdminDomainObject(dn);
		PersonObject personObject = ObjectCreator.createPersonObject(globalId);
		when(businessWebservice.createPerson(any(CreatePersonRequest.class))).thenReturn(2L);

		when(isimLdapManager.getPersonsByFilter(any(), any())).thenReturn(List.of(
				ldapPerson("dn1", "1"),
				ldapPerson("dn2", "1"),
				ldapPerson("dn3", "1")));
		when(personCreator.createPersonInIsim(any(), any())).thenReturn(Optional.of(dn));

		personService.insertPerson(adminDomainObject, personObject, false);

		ArgumentCaptor<PersonObject> personObjectArgumentCaptor = ArgumentCaptor.forClass(PersonObject.class);
		verify(personCreator).createPersonInIsim(any(), personObjectArgumentCaptor.capture());

		assertThat(personObjectArgumentCaptor.getValue().getLoginMethod()).isEqualTo("1");
	}

	@Test
	void insertPersonWithoutRrn() {
		String globalId = "99874563";
		Dn dn = ObjectCreator.getDn();
		AdminDomainObject adminDomainObject = ObjectCreator.createAdminDomainObject(dn);
		PersonObject personObject = ObjectCreator.createPersonObject(globalId);
		personObject.setNoRrn(true);

		when(businessWebservice.createPerson(any(CreatePersonRequest.class))).thenReturn(2L);

		when(personCreator.createPersonInIsim(any(), any())).thenReturn(Optional.of(dn));

		personService.insertPerson(adminDomainObject, personObject, false);

		ArgumentCaptor<PersonObject> personObjectArgumentCaptor = ArgumentCaptor.forClass(PersonObject.class);
		verify(personCreator).createPersonInIsim(any(), personObjectArgumentCaptor.capture());

		assertThat(personObjectArgumentCaptor.getValue().getLoginMethod()).isEqualTo("5");
	}

	@NotNull
	private IsimLdapPerson ldapPerson(String dn, String authenticationLevel) {
		return new IsimLdapPerson(
				new LdapAbstraction(new Dn(dn),
						new LdapAttributesBuilder()
								.attribute(IsimAttributeNames.ATTR_AUTHENTICATIONLEVEL, authenticationLevel)
								.build()));
	}
}
