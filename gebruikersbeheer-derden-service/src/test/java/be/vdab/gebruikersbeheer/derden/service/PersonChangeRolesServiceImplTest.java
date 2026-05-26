package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.util.ValidateUtils;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;
import java.util.Optional;

import static be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonChangeRolesServiceImplTest {
	private static final String PERSON_ID = "1";
	private static final String ROLE_ID = "2";
	private static final List<String> ROLE_IDS = List.of(ROLE_ID);
	@Mock
	AdminDomainService adminDomainService;
	@Mock
	AdminService adminService;
	@Mock
	ApplicationProperties applicationProperties;
	@Mock
	IsimUserContextManager isimUserContextManager;
	@Mock
	IsimLdapManager isimLdapManager;
	@Mock
	PersonService personService;
	@Mock
	RoleService roleService;
	@Mock
	RetryTemplate retryTemplate;
	@Mock
	ValidateUtils validateUtils;
	@Mock
	IsimWsSession isimWsSession;

	@Mock
	IsimLdapPerson isimLdapPerson;

	@InjectMocks
	PersonChangeRolesServiceImpl personChangeRolesService;

	@Test
	@DisplayName("""
			GIVEN person id doesnt exist
			WHEN changeRoles is called
			THEN RijksRegisternummerIsReedsIngebruikException will be thrown
			""")
	void changeRolesOfNonExistingPerson() {
		assertThatThrownBy(() -> personChangeRolesService.changeRoles(PERSON_ID, ROLE_IDS, null)).isInstanceOf(PersonNotFoundException.class);
	}

	@Test
	@DisplayName("""
			GIVEN person id exist, but admin domain is not found
			WHEN changeRoles is called to add a role
			THEN RijksRegisternummerIsReedsIngebruikException will be thrown
			""")
	void changeRolesOfNonExistigingVestiging() {
		var dnOfVestiging = Dn.of("erglobalid=7,");
		var personObject = new PersonObject();
		personObject.setParentDn(dnOfVestiging);
		when(personService.findPersonWithRolesByGlobalId(PERSON_ID)).thenReturn(Optional.of(personObject));
		when(this.adminDomainService.findAdminDomainByDnWithRoles(dnOfVestiging)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> personChangeRolesService.changeRoles(PERSON_ID, ROLE_IDS, null)).isInstanceOf(VestigingNietGevondenException.class);
	}

	@Test
	@DisplayName("""
			GIVEN person id exists, admin domain exists
			WHEN changeRoles is called to add a role
			THEN role added
			""")
	void addRole() {
		var dnOfVestiging = Dn.of("erglobalid=7,");
		var adminDomainObject = new AdminDomainObject();
		var personObject = new PersonObject();
		personObject.setParentDn(dnOfVestiging);
		var roleObject = new RoleObject();
		roleObject.setDn(Dn.of("erglobalid=" + ROLE_ID + ","));
		roleObject.setHasRole(false);
		personObject.setRoles(List.of(roleObject));
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(this.validateUtils.isCVSRole(roleObject)).thenReturn(false);
		when(roleService.getAantalAdmins(adminDomainObject)).thenReturn(0);
		when(isimLdapManager.getPersonByDn(personObject.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));
		when(roleService.personHasRole(isimLdapPerson, roleObject)).thenReturn(false);
		lenient().when(validateUtils.minAdminsReached(anyInt())).thenReturn(false);
		lenient().when(validateUtils.maxAdminsReached(anyInt())).thenReturn(false);
		when(personService.findPersonWithRolesByGlobalId(PERSON_ID)).thenReturn(Optional.of(personObject));
		when(this.adminDomainService.findAdminDomainByDnWithRoles(dnOfVestiging)).thenReturn(Optional.of(adminDomainObject));

		var result = personChangeRolesService.changeRoles(PERSON_ID, ROLE_IDS, null);

		assertThat(result.someRolesAreChanged()).isTrue();
		assertThat(result.minAdminsReached()).isFalse();
		assertThat(result.maxAdminsReached()).isFalse();

		verify(roleService).changePersonRoles(isimWsSession, isimLdapPerson, personObject, List.of(roleObject), List.of());
	}

	@Test
	@DisplayName("""
			GIVEN person id exists, admin domain exists
			WHEN changeRoles is called to change the cvsRole
			THEN cvsRole changed
			""")
	void changeCvsRole() {
		var dnOfVestiging = Dn.of("erglobalid=7,");
		var adminDomainObject = new AdminDomainObject();
		var personObject = new PersonObject();
		personObject.setParentDn(dnOfVestiging);
		personObject.setVdabCvsRol("testValue");
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(isimLdapManager.getPersonByDn(personObject.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));
		when(personService.findPersonWithRolesByGlobalId(PERSON_ID)).thenReturn(Optional.of(personObject));
		when(this.adminDomainService.findAdminDomainByDnWithRoles(dnOfVestiging)).thenReturn(Optional.of(adminDomainObject));

		var result = personChangeRolesService.changeRoles(PERSON_ID, emptyList(), "anotherTestValue");

		assertThat(result.someRolesAreChanged()).isTrue();
		assertThat(result.minAdminsReached()).isFalse();
		assertThat(result.maxAdminsReached()).isFalse();

		verify(roleService).changePersonRoles(isimWsSession, isimLdapPerson, personObject, emptyList(), List.of());
	}

	@Test
	@DisplayName("""
			GIVEN person id exists, admin domain exists
			WHEN changeRoles is called to add an admin role and max not reached
			THEN role added
			""")
	void addAdminRolRoleWhenMaxNotReached() {
		var dnOfVestiging = Dn.of("erglobalid=7,");
		var adminDomainObject = new AdminDomainObject();
		var personObject = new PersonObject();
		personObject.setParentDn(dnOfVestiging);
		var roleObject = new RoleObject();
		roleObject.setDn(Dn.of("erglobalid=" + ROLE_ID + ","));
		roleObject.setHasRole(false);
		roleObject.setAdminRole(true);
		personObject.setRoles(List.of(roleObject));
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		lenient().when(this.validateUtils.isCVSRole(roleObject)).thenReturn(false);
		when(roleService.getAantalAdmins(adminDomainObject)).thenReturn(0);
		when(isimLdapManager.getPersonByDn(personObject.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));
		when(roleService.personHasRole(isimLdapPerson, roleObject)).thenReturn(false);
		lenient().when(validateUtils.minAdminsReached(anyInt())).thenReturn(false);
		lenient().when(validateUtils.maxAdminsReached(anyInt())).thenReturn(false);
		when(personService.findPersonWithRolesByGlobalId(PERSON_ID)).thenReturn(Optional.of(personObject));
		when(this.adminDomainService.findAdminDomainByDnWithRoles(dnOfVestiging)).thenReturn(Optional.of(adminDomainObject));

		var result = personChangeRolesService.changeRoles(PERSON_ID, ROLE_IDS, null);

		assertThat(result.someRolesAreChanged()).isTrue();
		assertThat(result.minAdminsReached()).isFalse();
		assertThat(result.maxAdminsReached()).isFalse();
		verify(roleService).changePersonRoles(isimWsSession, isimLdapPerson, personObject, List.of(roleObject), List.of());
		verify(adminService).createNewAdmin(adminDomainObject, personObject);
	}

	@Test
	@DisplayName("""
			GIVEN person id exists, admin domain exists
			WHEN changeRoles is called to add an admin role and max reached
			THEN role not added
			""")
	void addAdminRolRoleWhenMaxReached() {
		var dnOfVestiging = Dn.of("erglobalid=7,");
		var adminDomainObject = new AdminDomainObject();
		var personObject = new PersonObject();
		personObject.setParentDn(dnOfVestiging);
		var roleObject = new RoleObject();
		roleObject.setDn(Dn.of("erglobalid=" + ROLE_ID + ","));
		roleObject.setHasRole(false);
		roleObject.setAdminRole(true);
		personObject.setRoles(List.of(roleObject));
		lenient().when(this.validateUtils.isCVSRole(roleObject)).thenReturn(false);
		when(roleService.getAantalAdmins(adminDomainObject)).thenReturn(0);
		when(isimLdapManager.getPersonByDn(personObject.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));
		when(roleService.personHasRole(isimLdapPerson, roleObject)).thenReturn(false);
		lenient().when(validateUtils.minAdminsReached(anyInt())).thenReturn(false);
		lenient().when(validateUtils.maxAdminsReached(anyInt())).thenReturn(true);
		when(personService.findPersonWithRolesByGlobalId(PERSON_ID)).thenReturn(Optional.of(personObject));
		when(this.adminDomainService.findAdminDomainByDnWithRoles(dnOfVestiging)).thenReturn(Optional.of(adminDomainObject));

		var result = personChangeRolesService.changeRoles(PERSON_ID, ROLE_IDS, null);

		assertThat(result.someRolesAreChanged()).isFalse();
		assertThat(result.minAdminsReached()).isFalse();
		assertThat(result.maxAdminsReached()).isTrue();
		verifyNoMoreInteractions(roleService);
	}

	@Test
	@DisplayName("""
			GIVEN person id exists, admin domain exists
			WHEN changeRoles is called to add a non pending cvs role
			THEN role added
			""")
	void addCvsRoleWhenNotPending() {
		var dnOfVestiging = Dn.of("erglobalid=7,");
		var adminDomainObject = new AdminDomainObject();
		var personObject = new PersonObject();
		personObject.setParentDn(dnOfVestiging);
		var roleObject = new RoleObject();
		roleObject.setDn(Dn.of("erglobalid=" + ROLE_ID + ","));
		roleObject.setHasRole(false);
		roleObject.setAdminRole(false);
		personObject.setRoles(List.of(roleObject));
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(this.validateUtils.isCVSRole(roleObject)).thenReturn(true);
		when(roleService.getAantalAdmins(adminDomainObject)).thenReturn(0);
		when(isimLdapManager.getPersonByDn(personObject.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));
		when(roleService.personHasRole(isimLdapPerson, roleObject)).thenReturn(false);
		when(personService.findPersonWithRolesByGlobalId(PERSON_ID)).thenReturn(Optional.of(personObject));
		when(this.adminDomainService.findAdminDomainByDnWithRoles(dnOfVestiging)).thenReturn(Optional.of(adminDomainObject));

		var result = personChangeRolesService.changeRoles(PERSON_ID, ROLE_IDS, null);

		assertThat(result.someRolesAreChanged()).isTrue();
		assertThat(result.minAdminsReached()).isFalse();
		assertThat(result.maxAdminsReached()).isFalse();
		verify(roleService).changePersonRole(isimWsSession, personObject, roleObject, true);
	}

	@Test
	@DisplayName("""
			GIVEN person id exists, admin domain exists
			WHEN changeRoles is called to add a pending cvs role
			THEN pending role NOT added
			""")
	void addCvsRoleWhenPending() {
		var dnOfVestiging = Dn.of("erglobalid=7,");
		var adminDomainObject = new AdminDomainObject();
		var personObject = new PersonObject();
		personObject.setParentDn(dnOfVestiging);
		var roleObject = new RoleObject();
		roleObject.setDn(Dn.of("erglobalid=" + ROLE_ID + ","));
		roleObject.setHasRole(false);
		roleObject.setAdminRole(false);
		roleObject.setPending(true);
		personObject.setRoles(List.of(roleObject));
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(this.validateUtils.isCVSRole(roleObject)).thenReturn(true);
		when(roleService.getAantalAdmins(adminDomainObject)).thenReturn(0);
		when(isimLdapManager.getPersonByDn(personObject.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));
		when(roleService.personHasRole(isimLdapPerson, roleObject)).thenReturn(false);
		when(personService.findPersonWithRolesByGlobalId(PERSON_ID)).thenReturn(Optional.of(personObject));
		when(this.adminDomainService.findAdminDomainByDnWithRoles(dnOfVestiging)).thenReturn(Optional.of(adminDomainObject));

		var result = personChangeRolesService.changeRoles(PERSON_ID, ROLE_IDS, null);

		assertThat(result.someRolesAreChanged()).isFalse();
		assertThat(result.minAdminsReached()).isFalse();
		assertThat(result.maxAdminsReached()).isFalse();
		verify(roleService, never()).changePersonRole(isimWsSession, personObject, roleObject, true);
	}

	@Test
	@DisplayName("""
			GIVEN person id exists, admin domain exists
			WHEN changeRoles is called to delete a role
			THEN role deleted
			""")
	void deleteRole() {
		var dnOfVestiging = Dn.of("erglobalid=7,");
		var adminDomainObject = new AdminDomainObject();
		var personObject = new PersonObject();
		personObject.setParentDn(dnOfVestiging);
		var roleObject = new RoleObject();
		roleObject.setDn(Dn.of("erglobalid=" + ROLE_ID + ","));
		roleObject.setHasRole(true);
		personObject.setRoles(List.of(roleObject));
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(roleService.getAantalAdmins(adminDomainObject)).thenReturn(0);
		when(isimLdapManager.getPersonByDn(personObject.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));
		lenient().when(validateUtils.minAdminsReached(anyInt())).thenReturn(false);
		lenient().when(validateUtils.maxAdminsReached(anyInt())).thenReturn(false);
		when(personService.findPersonWithRolesByGlobalId(PERSON_ID)).thenReturn(Optional.of(personObject));
		when(this.adminDomainService.findAdminDomainByDnWithRoles(dnOfVestiging)).thenReturn(Optional.of(adminDomainObject));

		var result = personChangeRolesService.changeRoles(PERSON_ID, List.of(), null);

		assertThat(result.someRolesAreChanged()).isTrue();
		assertThat(result.minAdminsReached()).isFalse();
		assertThat(result.maxAdminsReached()).isFalse();

		verify(roleService).changePersonRoles(isimWsSession, isimLdapPerson, personObject, List.of(), List.of(roleObject));
	}

	@Test
	@DisplayName("""
			GIVEN person id exists, admin domain exists
			WHEN changeRoles is called to delete an admin role and min not reached
			THEN role deleted
			""")
	void deleteAdminRole() {
		var dnOfVestiging = Dn.of("erglobalid=7,");
		var adminDomainObject = new AdminDomainObject();
		var personObject = new PersonObject();
		personObject.setParentDn(dnOfVestiging);
		var roleObject = new RoleObject();
		roleObject.setDn(Dn.of("erglobalid=" + ROLE_ID + ","));
		roleObject.setHasRole(true);
		roleObject.setAdminRole(true);
		personObject.setRoles(List.of(roleObject));
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(roleService.getAantalAdmins(adminDomainObject)).thenReturn(0);
		when(isimLdapManager.getPersonByDn(personObject.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));
		lenient().when(validateUtils.minAdminsReached(anyInt())).thenReturn(false);
		lenient().when(validateUtils.maxAdminsReached(anyInt())).thenReturn(false);
		when(personService.findPersonWithRolesByGlobalId(PERSON_ID)).thenReturn(Optional.of(personObject));
		when(this.adminDomainService.findAdminDomainByDnWithRoles(dnOfVestiging)).thenReturn(Optional.of(adminDomainObject));

		var result = personChangeRolesService.changeRoles(PERSON_ID, List.of(), null);

		assertThat(result.someRolesAreChanged()).isTrue();
		assertThat(result.minAdminsReached()).isFalse();
		assertThat(result.maxAdminsReached()).isFalse();

		verify(roleService).changePersonRoles(isimWsSession, isimLdapPerson, personObject, List.of(), List.of(roleObject));
		verify(adminService).deleteAdmin(adminDomainObject, personObject);
	}

	@Test
	@DisplayName("""
			GIVEN person id exists, admin domain exists
			WHEN changeRoles is called to delete an admin role and minimum reached
			THEN role NOT deleted
			""")
	void deleteAdminRoleWhenMinimumReached() {
		var dnOfVestiging = Dn.of("erglobalid=7,");
		var adminDomainObject = new AdminDomainObject();
		var personObject = new PersonObject();
		personObject.setParentDn(dnOfVestiging);
		var roleObject = new RoleObject();
		roleObject.setDn(Dn.of("erglobalid=" + ROLE_ID + ","));
		roleObject.setHasRole(true);
		roleObject.setAdminRole(true);
		personObject.setRoles(List.of(roleObject));
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(roleService.getAantalAdmins(adminDomainObject)).thenReturn(0);
		when(isimLdapManager.getPersonByDn(personObject.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));
		lenient().when(validateUtils.minAdminsReached(anyInt())).thenReturn(true);
		lenient().when(validateUtils.maxAdminsReached(anyInt())).thenReturn(false);
		when(personService.findPersonWithRolesByGlobalId(PERSON_ID)).thenReturn(Optional.of(personObject));
		when(this.adminDomainService.findAdminDomainByDnWithRoles(dnOfVestiging)).thenReturn(Optional.of(adminDomainObject));

		var result = personChangeRolesService.changeRoles(PERSON_ID, List.of(), null);

		assertThat(result.someRolesAreChanged()).isFalse();
		assertThat(result.minAdminsReached()).isTrue();
		assertThat(result.maxAdminsReached()).isFalse();

		verifyNoMoreInteractions(roleService);
		verify(adminService, never()).deleteAdmin(adminDomainObject, personObject);
	}
}