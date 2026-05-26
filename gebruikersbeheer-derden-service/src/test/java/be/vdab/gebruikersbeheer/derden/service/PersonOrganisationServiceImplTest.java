package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.converter.PersonConverter;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonOrganisationServiceImplTest {
	@Mock
	IsimLdapManager isimLdapManager;
	@Mock
	PersonConverter personConverter;

	@InjectMocks
	PersonOrganisationServiceImpl personOrganisationService;

	@Test
	void givenParentDnIsAdminDomainWhenExistsPersonInOrganizationThenReturnTrue() {
		Dn personDn = new Dn("p");
		Dn adminDomainDn = new Dn("p");
		when(isimLdapManager.getParentDn(personDn)).thenReturn(Optional.of(adminDomainDn));
		assertThat(personOrganisationService.existsPersonInOrganization(personDn, adminDomainDn)).isTrue();
	}

	@Test
	void givenParentDnIsNotAdminDomainWhenExistsPersonInOrganizationThenReturnFalse() {
		Dn personDn = new Dn("p");
		Dn adminDomainDn = new Dn("p");
		Dn other = new Dn("o");
		when(isimLdapManager.getParentDn(personDn)).thenReturn(Optional.of(other));
		assertThat(personOrganisationService.existsPersonInOrganization(personDn, adminDomainDn)).isFalse();
	}

	@Test
	void givenParentDnIsEmptyWhenExistsPersonInOrganizationThenReturnFalse() {
		Dn personDn = new Dn("p");
		Dn adminDomainDn = new Dn("p");
		when(isimLdapManager.getParentDn(personDn)).thenReturn(Optional.empty());
		assertThat(personOrganisationService.existsPersonInOrganization(personDn, adminDomainDn)).isFalse();
	}

	@Test
	void givenDnsExistWhenfindPersonsByDnThenReturnPersonObject() {
		Dn personDn = new Dn("p");
		IsimLdapPerson isimLdapPerson = new IsimLdapPerson(null);
		PersonObject personObject = new PersonObject();
		List<PersonObject> personsToFind = Collections.singletonList(personObject);
		when(isimLdapManager.getPersonByDn(personDn, IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));
		when(personConverter.convertList(Collections.singletonList(isimLdapPerson), false)).thenReturn(personsToFind);
		assertThat(personOrganisationService.findPersonsByDn(Collections.singletonList(personDn))).isEqualTo(personsToFind);
	}
}
