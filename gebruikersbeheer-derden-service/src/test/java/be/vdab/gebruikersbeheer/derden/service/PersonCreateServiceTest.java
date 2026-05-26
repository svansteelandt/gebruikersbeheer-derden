package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.GebruikerBestaatReedsOpDezeVestigingException;
import be.vdab.gebruikersbeheer.derden.exception.GebruikerCreateFoutException;
import be.vdab.gebruikersbeheer.derden.exception.PersonCreateValidationException;
import be.vdab.gebruikersbeheer.derden.exception.RijksRegisternummerIsReedsIngebruikException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import be.vdab.gebruikersbeheer.derden.extern.validator.PersonFormValidator;
import be.vdab.gebruikersbeheer.derden.security.SecurityExpressions;
import be.vdab.gebruikersbeheer.derden.service.cache.CacheService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonCreateServiceTest {
	
	private static final String VESTIGING_ID = "1";
	@Mock
	PersonFormValidator personFormValidator;
	@Mock
	AdminDomainService adminDomainService;
	@Mock
	ApplicationProperties applicationProperties;
	@Mock
	PersonService personService;
	@Mock
	RoleService roleService;
	@Mock
	CacheService cacheService;
	@Mock
	SecurityExpressions securityExpressions;
	@Mock
	BindingResult bindingResult;

	@InjectMocks
	PersonCreateServiceImpl personCreateService;

	@Test
	@DisplayName("""
			GIVEN person already exists with insz
			WHEN create is called
			THEN RijksRegisternummerIsReedsIngebruikException will be thrown
			""")
	void canNotCreateAPersonWithRrnrIfPersonAlreadyExists() {
		var personToCreate = new PersonObject();
		personToCreate.setNoRrn(false);
		personToCreate.setNationalNumber("n");
		var adminDomainObject = new AdminDomainObject();
		adminDomainObject.setDn(Dn.of("1"));
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(adminDomainObject));
		when(personService.rrnExists(personToCreate.getNationalNumber(), adminDomainObject.getDn())).thenReturn(true);

		assertThatThrownBy(() -> personCreateService.create(VESTIGING_ID, personToCreate, List.of())).isInstanceOf(RijksRegisternummerIsReedsIngebruikException.class);
	}

	@Test
	@DisplayName("""
			GIVEN person data is invalid
			WHEN create is called
			THEN PersonCreateValidationException will be thrown
			""")
	void willThrowBindingExceptionIfValidationOfContentFailes() {
		var personToCreate = new PersonObject();
		personToCreate.setNoRrn(false);
		personToCreate.setNationalNumber("n");
		var adminDomainObject = new AdminDomainObject();
		adminDomainObject.setDn(Dn.of("1"));
		adminDomainObject.setIkp(Ikp.of(203000L));
		doAnswer(invocation -> {
			((BindingResult) invocation.getArguments()[1]).reject("testFout");
			return null;
		}).when(personFormValidator).validate(eq(personToCreate), any());
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(adminDomainObject));

		assertThatThrownBy(() -> personCreateService.create(VESTIGING_ID, personToCreate, List.of())).isInstanceOf(PersonCreateValidationException.class);
	}

	@Test
	@DisplayName("""
			GIVEN person already exists with rrnr for vestiging in recycle bin
			WHEN create is called
			THEN GebruikerBestaatReedsOpDezeVestigingException will be thrown
			""")
	void canNotCreateAPersonWithRrnrIfPersonAlreadyExistsForVestigingInRecycleBin() {
		var personToCreate = new PersonObject();
		personToCreate.setNoRrn(false);
		personToCreate.setNationalNumber("n");
		var adminDomainObject = new AdminDomainObject();
		adminDomainObject.setDn(Dn.of("1"));
		adminDomainObject.setIkp(Ikp.of(203000L));
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(adminDomainObject));
		when(personService.rrnExists(personToCreate.getNationalNumber(), adminDomainObject.getDn())).thenReturn(false);
		when(personService.getPersonDnInPrullenbakForRrnAndIkpNummer(personToCreate.getNationalNumber(), adminDomainObject.getIkp())).thenReturn(Dn.of("2"));

		assertThatThrownBy(() -> personCreateService.create(VESTIGING_ID, personToCreate, List.of())).isInstanceOf(GebruikerBestaatReedsOpDezeVestigingException.class);
	}

	@Test
	@DisplayName("""
			GIVEN data is ok for validation
			WHEN create is called but insert throws exception 
			THEN GebruikerCreateFoutException will be thrown
			""")
	void willThrowGebruikerCreateFoutExceptionIfExceptionDuringInsert() {
		var personToCreate = new PersonObject();
		personToCreate.setNoRrn(false);
		personToCreate.setNationalNumber("n");
		var adminDomainObject = new AdminDomainObject();
		adminDomainObject.setDn(Dn.of("1"));
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(adminDomainObject));
		when(personService.rrnExists(personToCreate.getNationalNumber(), adminDomainObject.getDn())).thenReturn(false);
		when(personService.insertPerson(adminDomainObject, personToCreate, false)).thenThrow(new NullPointerException("test"));

		initAdminRole();

		assertThatThrownBy(() -> personCreateService.create(VESTIGING_ID, personToCreate, List.of())).isInstanceOf(GebruikerCreateFoutException.class);
	}

	@Test
	@DisplayName("""
			GIVEN person with rrnr data is ok and no roles and is first user of admin domain
			WHEN create is called
			THEN person will be created with admin role and returned
			""")
	void canCreateAPersonWithRrnrWithoutRolesIfAllValidationIsOk() throws Exception {
		var personToCreate = new PersonObject();
		personToCreate.setNoRrn(false);
		personToCreate.setNationalNumber("n");
		var adminDomainObject = new AdminDomainObject();
		adminDomainObject.setDn(Dn.of("1"));
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(adminDomainObject));
		when(personService.rrnExists(personToCreate.getNationalNumber(), adminDomainObject.getDn())).thenReturn(false);

		RoleObject adminRole = initAdminRole();
		adminRole.setHasRole(true);

		var resultRoles = List.of(adminRole);

		Dn personDn = new Dn("erglobalid=1,ou=0,ou=people");
		var personCreated = new PersonObject(personDn);

		var personWithRoles = new PersonObject(personCreated);
		personWithRoles.setRoles(resultRoles);

		when(personService.findPersonByDn(personDn, adminDomainObject))
				.thenReturn(Optional.of(personCreated))     // Eerste aanroep na creatie (zonder rollen)
				.thenReturn(Optional.of(personWithRoles));  // Tweede aanroep na toekenning rollen

		when(personService.insertPerson(adminDomainObject, personToCreate, false)).thenReturn(personCreated.getDn());

		var result = personCreateService.create(VESTIGING_ID, personToCreate, List.of());
		verify(personService).insertPerson(adminDomainObject, personToCreate, false);
		assertThat(result).isEqualTo(personCreated);
		assertThat(result.getRoles()).isEqualTo(resultRoles);
		verify(cacheService).deleteFromPersonCaches(personCreated.getDn());
	}

	@Test
	@DisplayName("""
			GIVEN person with rrnr data is ok and roles, without admin and is first user of admindomain
			WHEN create is called
			THEN person will be created with roles and returned
			""")
	void canCreateAPersonWithRrnrWithRolesIfAllValidationIsOk() throws Exception {
		var personToCreate = new PersonObject();
		personToCreate.setNoRrn(false);
		personToCreate.setNationalNumber("n");
		var adminDomainObject = new AdminDomainObject();
		adminDomainObject.setDn(Dn.of("1"));
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(adminDomainObject));
		when(personService.rrnExists(personToCreate.getNationalNumber(), adminDomainObject.getDn())).thenReturn(false);

		RoleObject adminRole = initAdminRole();
		adminRole.setHasRole(true);

		var role = initRole("321", "Test Role");
		role.setHasRole(true);

		var rolesToAdd = List.of(role);
		var resultRoles = List.of(role, adminRole);

		Dn personDn = new Dn("erglobalid=1,ou=0,ou=people");
		var personCreated = new PersonObject(personDn);

		var personWithRoles = new PersonObject(personCreated);
		personWithRoles.setRoles(resultRoles);

		when(personService.findPersonByDn(personDn, adminDomainObject))
				.thenReturn(Optional.of(personCreated))     // Eerste aanroep na creatie (zonder rollen)
				.thenReturn(Optional.of(personWithRoles));  // Tweede aanroep na toekenning rollen

		when(personService.insertPerson(adminDomainObject, personToCreate, false)).thenReturn(personCreated.getDn());

		var result = personCreateService.create(VESTIGING_ID, personToCreate, rolesToAdd);

		verify(personService).insertPerson(adminDomainObject, personToCreate, false);
		verify(roleService).addAndRemoveRoles(personCreated, adminDomainObject);
		assertThat(result).isEqualTo(personCreated);
		assertThat(personCreated.getRoles()).isEqualTo(resultRoles);
		verify(cacheService).deleteFromPersonCaches(personCreated.getDn());
	}

	@Test
	@DisplayName("""
			GIVEN person without rrnr data is ok, has no roles and is first user of admindomain
			WHEN create is called
			THEN person will be created with admin role and returned
			""")
	void canCreateAPersonWithoutRrnrWithoutRolesIfAllValidationIsOk() throws Exception {
		var personToCreate = new PersonObject();
		personToCreate.setNoRrn(true);
		var adminDomainObject = new AdminDomainObject();
		adminDomainObject.setDn(Dn.of("1"));
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(adminDomainObject));
		var personCreated = new PersonObject();
		when(personService.findPersonByDn(any(), eq(adminDomainObject))).thenReturn(Optional.of(personCreated));

		RoleObject adminRole = initAdminRole();

		var resultRoles = List.of(adminRole);

		var result = personCreateService.create(VESTIGING_ID, personToCreate, List.of());
		verify(personService).insertPerson(adminDomainObject, personToCreate, false);
		verify(personService, never()).rrnExists(personToCreate.getNationalNumber(), adminDomainObject.getDn());
		assertThat(result).isEqualTo(personCreated);
		assertThat(result.getRoles()).isEqualTo(resultRoles);
	}

	@Test
	@DisplayName("""
			GIVEN person with rrnr data is ok, has admin role and is first user of admindomain
			WHEN create is called
			THEN person will be created with admin role and returned
			""")
	void canCreateFirstGebruikerWithRrnAndWithAdminrole() throws Exception {
		when(personService.findPersonsFromOrganization(any(Dn.class))).thenReturn(Collections.emptyList());

		RoleObject adminRole = initAdminRole();

		RoleObject userAdminRole = new RoleObject(adminRole);
		userAdminRole.setHasRole(true);

		createGebruikerWithRrn(Collections.singletonList(userAdminRole), 1);
	}

	@Test
	@DisplayName("""
			GIVEN person with rrnr data is ok, has no admin role and is not first user of admindomain
			WHEN create is called
			THEN person will be created without admin role and returned
			""")
	void createNotFirstGebruiker() throws Exception {
		when(personService.findPersonsFromOrganization(any(Dn.class))).thenReturn(List.of(Dn.of("erglobalid=123")));

		createGebruikerWithRrn(List.of(), 0);
	}

	void createGebruikerWithRrn(List<RoleObject> rolesToAdd, int resultUserRoleCount) throws RijksRegisternummerIsReedsIngebruikException, GebruikerCreateFoutException, PersonCreateValidationException, GebruikerBestaatReedsOpDezeVestigingException {
		var personToCreate = new PersonObject();
		personToCreate.setNoRrn(false);
		personToCreate.setNationalNumber("n");

		var adminDomainObject = new AdminDomainObject();
		adminDomainObject.setDn(Dn.of("1"));
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(adminDomainObject));

		var personCreated = new PersonObject();
		when(personService.findPersonByDn(any(), eq(adminDomainObject))).thenReturn(Optional.of(personCreated));

		var result = personCreateService.create(VESTIGING_ID, personToCreate, rolesToAdd);
		verify(personService).insertPerson(adminDomainObject, personToCreate, resultUserRoleCount == 0);
		verify(personService).rrnExists(personToCreate.getNationalNumber(), adminDomainObject.getDn());

		assertThat(result).isEqualTo(personCreated);
		assertThat(result.getRoles()).hasSize(resultUserRoleCount);
	}

	@Test
	@DisplayName("""
			GIVEN vestiging id does not exist
			WHEN create user is called
			THEN VestigingNietGevondenException will be thrown
			""")
	void willThrowVestigingNotFoundOfVestigingDoesNotExist() {
		when(adminDomainService.findAdminDomainByDnWithRoles(this.applicationProperties.createAdminDomainDn(VESTIGING_ID))).thenReturn(Optional.empty());

		var personToCreate = new PersonObject();
		assertThatThrownBy(() -> personCreateService.create(VESTIGING_ID, personToCreate, List.of())).isInstanceOf(VestigingNietGevondenException.class);
	}

	private RoleObject initAdminRole() {
		RoleObject adminRole = new RoleObject(Dn.of("erglobalid=123,ou=roles,erglobalid=00000000000000000000,ou=vdab,O=VDAB,C=BE"));
		adminRole.setRoleName("DOMAIN ADMINS");
		adminRole.setVdabRoleName("DOMAIN ADMINS");
		adminRole.setAdminRole(true);

		when(roleService.findAdminRole()).thenReturn(adminRole);

		return adminRole;
	}

	private RoleObject initRole(String globalId, String roleName) {
		RoleObject role = new RoleObject(Dn.of("erglobalid=%s,ou=roles,erglobalid=00000000000000000000,ou=vdab,O=VDAB,C=BE".formatted(globalId)));
		role.setRoleName(roleName);
		role.setVdabRoleName(roleName);
		role.setAdminRole(false);

		return role;
	}
}