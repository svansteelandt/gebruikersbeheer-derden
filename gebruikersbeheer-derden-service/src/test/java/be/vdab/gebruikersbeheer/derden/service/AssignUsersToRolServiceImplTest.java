package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.MaximumAantalAdminsOverschredenException;
import be.vdab.gebruikersbeheer.derden.exception.MinimumAantalAdminsNietGehaaldException;
import be.vdab.gebruikersbeheer.derden.exception.RoleNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignUsersToRolServiceImplTest {
	private static final String VESTIGING_ID = "1";


	@Mock
	AdminDomainService adminDomainService;
	@Mock
	ApplicationProperties applicationProperties;
	@Mock
	RoleService roleService;
	@Mock
	PersonService personService;

	@InjectMocks
	AssignUsersToRolServiceImpl assignUsersToRolService;

	final String ORGANISATION_ID = "o1";
	final String ROLE_ID = "r1";
	final String USER_ID1 = "u1";
	final String USER_ID2 = "u2";

	@Test
	@DisplayName("""
			GIVEN niet bestaande vestigingId
			WHEN assign is called
			THEN VestigingNietGevondenException will be thrown
			""")
	void vestigingBestaatNiet() {
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.empty());
		var role = createRole();
		role.setAdminRole(true);
		assertThatThrownBy(() -> assignUsersToRolService.assign(ORGANISATION_ID, ROLE_ID, List.of())).isInstanceOf(VestigingNietGevondenException.class);
	}

	@Test
	@DisplayName("""
			GIVEN niet bestaande rolId
			WHEN assign is called
			THEN RoleNotFoundException will be thrown
			""")
	void rolBestaatNiet() {
		var vestiging = new AdminDomainObject();
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(vestiging));
		when(roleService.findRoleByGlobalId(ROLE_ID)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> assignUsersToRolService.assign(ORGANISATION_ID, ROLE_ID, List.of())).isInstanceOf(RoleNotFoundException.class);
	}

	@Test
	@DisplayName("""
			GIVEN min 1 & max 2 admin properties and 0 users who get admin rol
			WHEN assign is called
			THEN MinimumAantalAdminsNietGehaaldException will be thrown
			""")
	void minimumAdmins() {
		when(applicationProperties.getMinDomainAdmins()).thenReturn(1);
		var vestiging = new AdminDomainObject();
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(vestiging));
		var role = createRole();
		role.setAdminRole(true);
		when(roleService.findRoleByGlobalId(ROLE_ID)).thenReturn(Optional.of(role));
		assertThatThrownBy(() -> assignUsersToRolService.assign(ORGANISATION_ID, ROLE_ID, List.of())).isInstanceOf(MinimumAantalAdminsNietGehaaldException.class);
	}

	@Test
	@DisplayName("""
			GIVEN min 1 & max 2 admin properties and 3 users who get admin rol
			WHEN assign is called
			THEN MaximumAantalAdminsOverschredenException will be thrown
			""")
	void maximumAdmins() {
		when(applicationProperties.getMinDomainAdmins()).thenReturn(1);
		when(applicationProperties.getMaxDomainAdmins()).thenReturn(2);
		var vestiging = new AdminDomainObject();
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(vestiging));
		var role = createRole();
		role.setAdminRole(true);
		when(roleService.findRoleByGlobalId(ROLE_ID)).thenReturn(Optional.of(role));
		assertThatThrownBy(() -> assignUsersToRolService.assign(ORGANISATION_ID, ROLE_ID, List.of("1", "2", "3"))).isInstanceOf(MaximumAantalAdminsOverschredenException.class);
	}

	@Test
	@DisplayName("""
			GIVEN min 1 & max 2 admin properties and 2 users who get admin rol
			WHEN assign is called
			THEN role service is called to add role to users
			AND cache is cleared for users who are changed
			""")
	void assignAdminRolToUsers() throws Exception {
		when(applicationProperties.getMinDomainAdmins()).thenReturn(1);
		when(applicationProperties.getMaxDomainAdmins()).thenReturn(2);
		var vestiging = new AdminDomainObject();
		vestiging.setDn(Dn.of(VESTIGING_ID));
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(vestiging));
		var role = createRole();
		role.setAdminRole(true);
		when(roleService.findRoleByGlobalId(ROLE_ID)).thenReturn(Optional.of(role));
		var person1 = new PersonObject();
		person1.setDn(Dn.of("erglobalid=1,"));
		var person2 = new PersonObject();
		person2.setChanged(true);
		person2.setDn(Dn.of("erglobalid=2,"));
		var person3 = new PersonObject();
		person3.setDn(Dn.of("erglobalid=3,"));
		var usersOfVestiging = List.of(person1, person2, person3);
		when(personService.findPersonsFromOrganizationDnWithRoleWithPending(vestiging.getDn(), role)).thenReturn(usersOfVestiging);
		assignUsersToRolService.assign(ORGANISATION_ID, ROLE_ID, List.of(USER_ID1, USER_ID2));

		verifySuccessFullRoleAssign(usersOfVestiging, role, vestiging, person2);
	}

	private void verifySuccessFullRoleAssign(List<PersonObject> usersOfVestiging, RoleObject role, AdminDomainObject vestiging, PersonObject person2) {
		verify(roleService).changePersonRoleChangeList(usersOfVestiging, role, vestiging);
		verify(personService).updatePersonCaches(person2.getDn(), vestiging);
		verifyNoMoreInteractions(personService);
	}

	@Test
	@DisplayName("""
			GIVEN 3 users who get normal rol
			WHEN assign is called
			THEN role service is called to add role to users
			AND cache is cleared for users who are changed
			""")
	void assignNormalRolToUsers() throws Exception {
		var vestiging = new AdminDomainObject();
		vestiging.setDn(Dn.of(VESTIGING_ID));
		when(adminDomainService.findAdminDomainByDnWithRoles(any())).thenReturn(Optional.of(vestiging));
		var role = createRole();
		role.setAdminRole(false);
		when(roleService.findRoleByGlobalId(ROLE_ID)).thenReturn(Optional.of(role));
		var person1 = new PersonObject();
		person1.setDn(Dn.of("erglobalid=1,"));
		person1.setChanged(true);
		var usersOfVestiging = List.of(person1);
		when(personService.findPersonsFromOrganizationDnWithRoleWithPending(vestiging.getDn(), role)).thenReturn(usersOfVestiging);
		assignUsersToRolService.assign(ORGANISATION_ID, ROLE_ID, List.of(USER_ID1, USER_ID2));

		verifySuccessFullRoleAssign(usersOfVestiging, role, vestiging, person1);
	}

	private static RoleObject createRole() {
		var roleObject = new RoleObject();
		roleObject.setDn(Dn.of("erglobalid=1,"));
		roleObject.setVdabRoleName("vdab rol naam");
		roleObject.setVdabRoleDescription("vdab rol omschrijving");
		return roleObject;
	}
}