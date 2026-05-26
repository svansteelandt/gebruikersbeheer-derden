package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.converter.PersonConverter;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.service.cache.CacheService;
import be.vdab.gebruikersbeheer.derden.web.internal.v1.Insz;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.exception.OrganizationNotFoundException;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.LdapFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

	@Mock
	PersonConverter personConverter;

	@Mock
	IsimLdapManager isimLdapManager;

	@Mock
	AdminDomainService adminDomainService;

	@Mock
	IsimWsClient isimWsClient;

	@Mock
	TaskService taskService;

	@Mock
	CacheService cacheService;

	@Mock
	CacheManager cacheManager;

	@Mock
	PersonOrganizationService personOrganizationService;

	@Mock
	IsimWsSession isimWsSession;
	@Mock
	IsimUserContextManager isimUserContextManager;

	@Mock
	ApplicationProperties applicationProperties;

	@InjectMocks
	PersonServiceImpl personService;

	@Captor
	private ArgumentCaptor<LdapFilter> ldapFilterCaptor;

	@Test
	void getGlobalIdFromPersonBy_inszDoesNotExist_throwPersonNotFoundException() {
		String orgId = "123";
		Dn dn = ObjectCreator.getDn();
		AdminDomainObject adminDomainObject = ObjectCreator.createAdminDomainObject(dn);
		when(adminDomainService.findAdminDomainByOrgId(orgId)).thenReturn(Optional.of(adminDomainObject));
		when(isimLdapManager.getPersonsForAdminDomain(dn, DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Collections.emptyList());
		Insz insz = Insz.builder().inszNummer("insz").build();

		assertThrows(PersonNotFoundException.class,
				() -> personService.getGlobalIdFromPersonBy("123", insz));
	}

	@Test
	void getGlobalIdFromPersonBy_organizationDoesNotExist_throwOrganizationNotFoundException() {
		String orgId = "123";
		when(adminDomainService.findAdminDomainByOrgId(orgId)).thenReturn(Optional.empty());
		Insz insz = Insz.builder().inszNummer("insz").build();

		assertThrows(OrganizationNotFoundException.class,
				() -> personService.getGlobalIdFromPersonBy(orgId, insz));
	}

	@Test
	void getGlobalIdFromPersonBy_validOrgIdAndInsz_returnsGlobalId() {
		String orgId = "123";
		String globalId = "99874563";
		String inszNumber = "insz";

		Dn dn = ObjectCreator.getDn();
		Insz insz = Insz.builder().inszNummer(inszNumber).build();
		AdminDomainObject adminDomainObject = ObjectCreator.createAdminDomainObject(dn);

		IsimLdapPerson person = mock(IsimLdapPerson.class); //FIXME Person should be accessible so I don't have to mock a DTO.
		when(person.getRijksregisternummer()).thenReturn(inszNumber);
		when(adminDomainService.findAdminDomainByOrgId(orgId)).thenReturn(Optional.of(adminDomainObject));
		when(isimLdapManager.getPersonsForAdminDomain(dn, DEFAULT_PERSON_ATTRIBUTES_DERDEN))
				.thenReturn(Collections.singletonList(person));
		when(personConverter.convert(person)).thenReturn(ObjectCreator.createPersonObject(globalId));

		assertThat(personService.getGlobalIdFromPersonBy(orgId, insz)).isEqualTo(globalId);
	}

	@Test
	void findPersonsFromOrganizationByDn() {
		Dn dn = ObjectCreator.getDn();
		List<Dn> DN_LIST = new ArrayList<>();
		DN_LIST.add(Dn.of("erglobalid=123,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be"));
		DN_LIST.add(Dn.of("erglobalid=456,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be"));

		when(isimLdapManager.getPersonDnsForAdminDomain(dn)).thenReturn(DN_LIST);

		List<Dn> dns = this.personService.findPersonsFromOrganization(dn);

		assertThat(dns).isNotNull().isEqualTo(DN_LIST);
	}

	@Test
	void findPersonsFromOrganizationByAdminObject() {
		Dn adminDomainDn = ObjectCreator.getDn();
		AdminDomainObject adminDomainObject = ObjectCreator.createAdminDomainObject(adminDomainDn);
		Map<String, RoleObject> rolesMap = adminDomainObject.getRoles().stream().collect(Collectors.toMap(role -> role.getDn().getGlobalId(), role -> role));


		List<IsimLdapPerson> persons = List.of(mock(IsimLdapPerson.class));
		List<PersonObject> personObjects = List.of(mock(PersonObject.class));
		when(isimLdapManager.getPersonsForAdminDomain(eq(adminDomainDn), eq(IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN))).thenReturn(persons);
		when(personConverter.convertList(persons, rolesMap, false)).thenReturn(personObjects);

		List<PersonObject> result = this.personService.findPersonsFromOrganization(adminDomainObject);
		assertThat(result).isEqualTo(personObjects);
	}

	@Test
	void findNonMlpOpleidingPersonsFromOrganizationByAdminObject() {
		Dn adminDomainDn = ObjectCreator.getDn();
		AdminDomainObject adminDomainObject = ObjectCreator.createAdminDomainObject(adminDomainDn);
		Map<String, RoleObject> rolesMap = adminDomainObject.getRoles().stream().collect(Collectors.toMap(role -> role.getDn().getGlobalId(), role -> role));


		List<IsimLdapPerson> persons = List.of(mock(IsimLdapPerson.class));
		List<PersonObject> personObjects = List.of(mock(PersonObject.class));
		when(isimLdapManager.getPersonsForAdminDomain(eq(adminDomainDn), eq(IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN), ldapFilterCaptor.capture()))
				.thenReturn(persons);
		when(personConverter.convertList(persons, rolesMap, false)).thenReturn(personObjects);

		List<PersonObject> result = this.personService.findNonMlpOpleidingPersonsFromOrganization(adminDomainObject);
		assertThat(result).isEqualTo(personObjects);

		assertThat(ldapFilterCaptor.getValue().getFilter()).isEqualTo("(!(employeetype=MlpOpleiding))");
	}

	@Test
	void findPersonByDn() {
		Dn dn = ObjectCreator.getDn();
		PersonObject personObject = new PersonObject();
		AdminDomainObject adminDomainObject = ObjectCreator.createAdminDomainObject(dn);
		IsimLdapPerson isimLdapPerson = new IsimLdapPerson(null);
		when(isimLdapManager.getPersonByDn(dn, IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));
		when(personConverter.convert(isimLdapPerson)).thenReturn(personObject);
		Optional<PersonObject> person = this.personService.findPersonByDn(dn, adminDomainObject);
		assertThat(person).isPresent();
	}

	@Test
	@DisplayName("""
			WHEN searching person by gebruikersnaam
			THEN returns personobject for gebruikersnaam
			""")
	void findPersonByGebruikersnaam() {
		String gebruikersnaam = initPersonByGebruikersnaam();

		checkFindPersonByGebruikersnaam(gebruikersnaam, this.personService.findPersonByGebruikersnaam(gebruikersnaam));
	}

	@Test
	@DisplayName("""
			WHEN searching person by gebruikersnaam with list of attributes
			THEN returns personobject for gebruikersnaam with asked attributes
			""")
	void findPersonByGebruikersnaamWithAttributeList() {
		String gebruikersnaam = initPersonByGebruikersnaam();

		checkFindPersonByGebruikersnaam(gebruikersnaam, this.personService.findPersonByGebruikersnaam(gebruikersnaam, List.of()));
	}

	private String initPersonByGebruikersnaam() {
		String gebruikersnaam = "TEST_TRUSTCENTER";
		PersonObject personObject = new PersonObject();
		personObject.setUserId(gebruikersnaam);

		IsimLdapPerson isimLdapPerson = new IsimLdapPerson(null);
		when(isimLdapManager.getPersonByUid(anyString(), anyList())).thenReturn(Optional.of(isimLdapPerson));
		when(personConverter.convert(isimLdapPerson, false)).thenReturn(personObject);

		return gebruikersnaam;
	}

	void checkFindPersonByGebruikersnaam(String gebruikersnaam, PersonObject result) {
		assertThat(result).isNotNull();
		assertThat(result.getUserId()).isEqualTo(gebruikersnaam);
	}

	@Test
	void deletePerson() throws Exception {
		String globalId = "99874563";
		Dn dn = ObjectCreator.getDn();
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		AdminDomainObject adminDomainObject = ObjectCreator.createAdminDomainObject(dn);
		PersonObject personObject = ObjectCreator.createPersonObject(globalId);

		assertThat(this.personService.deletePerson(adminDomainObject, personObject)).isTrue();

		verify(isimWsClient).deletePerson(isimWsSession, personObject.getDn());
		verify(cacheService).deleteFromPersonCaches(personObject);
	}

	@Test
	void findPersons() {
		var query = FindPersonQuery.builder()
				.gebruikersnaam("Test")
				.rijksregisternummer("78021020976")
				.voornaam("Test")
				.naam("Test")
				.volledigeNaam("Test")
				.email("Test@test.be")
				.oe("10008045")
				.build();
		List<PersonObject> persons = this.personService.findPersons(query);
		assertThat(persons).isNotNull();
	}

	@Test
	void findPersonWithRolesByGlobalIdWhenPersonExists() {
		var vestiging = new AdminDomainObject();
		var gebruikerId = "1";
		var gebruikerDn = Dn.of(gebruikerId);
		var vestigingDn = Dn.of("2");
		var personObject = new PersonObject();
		personObject.setParentDn(vestigingDn);
		var isimLdapPerson = mock(IsimLdapPerson.class);
		when(applicationProperties.createPersonDn(gebruikerId)).thenReturn(gebruikerDn);
		when(isimLdapManager.getPersonByDn(gebruikerDn, DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));
		when(personConverter.convert(isimLdapPerson)).thenReturn(personObject);
		when(adminDomainService.findAdminDomainByDnWithRoles(personObject.getParentDn())).thenReturn(Optional.of(vestiging));
		var person = this.personService.findPersonWithRolesByGlobalId(gebruikerId);
		assertThat(person).isPresent().hasValue(personObject);
	}

	@Test
	void findPersonWithRolesByGlobalIdWhenPersonDoesNotExist() {
		var gebruikerId = "1";
		var gebruikerDn = Dn.of(gebruikerId);
		when(applicationProperties.createPersonDn(gebruikerId)).thenReturn(gebruikerDn);
		when(isimLdapManager.getPersonByDn(gebruikerDn, DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.empty());
		Optional<PersonObject> person = this.personService.findPersonWithRolesByGlobalId(gebruikerId);
		assertThat(person).isEmpty();
	}

}