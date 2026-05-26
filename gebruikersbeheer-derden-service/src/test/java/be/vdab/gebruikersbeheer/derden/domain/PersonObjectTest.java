package be.vdab.gebruikersbeheer.derden.domain;

import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PersonObjectTest {

    private static final String DELETE_DESCRIPTION = "Stout geweest";

    @Test
    void getDeleteDescription() {
        PersonObject personObject = new PersonObject();
        personObject.setDeleteDescription(DELETE_DESCRIPTION);

        assertThat(personObject.getDeleteDescription()).isEqualTo(DELETE_DESCRIPTION);
    }

    @Test
    void getDeleteDescription_empty() {
        PersonObject personObject = new PersonObject();
        personObject.setDeleteDescription("");

        assertThat(personObject.getDeleteDescription()).isEqualTo("Onbekend");
    }

    @Test
    void getHasRole() {
        PersonObject personObject = new PersonObject();
        assertThat(personObject.getHasRole()).isFalse();

        personObject.setHasRole(true);
        assertThat(personObject.getHasRole()).isTrue();
    }

	@Test
	void getFullname() {
		PersonObject personObject = new PersonObject();

		personObject.setFirstName("John");
		personObject.setLastName("Doe");
		assertThat(personObject.getFullName()).isEqualTo("John Doe");

		personObject.setFirstName("John");
		personObject.setLastName(null);
		assertThat(personObject.getFullName()).isEqualTo("John");

		personObject.setFirstName(null);
		personObject.setLastName("Doe");
		assertThat(personObject.getFullName()).isEqualTo("Doe");

		personObject.setFirstName(null);
		personObject.setLastName(null);
		assertThat(personObject.getFullName()).isEqualTo(null);

		// not sure about these next two assertions
		personObject.setFirstName("");
		personObject.setLastName(null);
		assertThat(personObject.getFullName()).isEqualTo("");

		personObject.setFirstName(null);
		personObject.setLastName("");
		assertThat(personObject.getFullName()).isEqualTo("");
	}

	@Test
	void hasRole() {
		PersonObject personObject = new PersonObject();
		personObject.setRoles(List.of(
				roleWithName("role1"),
				roleWithName("role2"),
				roleWithName("role3")
		));
		assertThat(personObject.hasRole("role1")).isTrue();
		assertThat(personObject.hasRole("role2")).isTrue();
		assertThat(personObject.hasRole("role3")).isTrue();
		assertThat(personObject.hasRole("Role1")).isFalse();
		assertThat(personObject.hasRole("role4")).isFalse();

		personObject.setRoles(List.of());
		assertThat(personObject.hasRole("role1")).isFalse();

		personObject.setRoles(null);
		assertThat(personObject.hasRole("role1")).isFalse();
	}

	@Test
	void getNationalNumberFormatted() {
		PersonObject personObject = new PersonObject();

		assertThat(personObject.getNationalNumberFormatted()).isEqualTo("");

		personObject.setNationalNumber("90122718707");
		assertThat(personObject.getNationalNumberFormatted()).isEqualTo("90.12.27-187.07");

		personObject.setNationalNumber("1234");
		assertThat(personObject.getNationalNumberFormatted()).isEqualTo("1234");
	}

	@Test
	void getDisplayName() {
		PersonObject personObject = new PersonObject();

		assertThat(personObject.getDisplayRole()).isEqualTo("");

		personObject.setRoles(List.of());

		assertThat(personObject.getDisplayRole()).isEqualTo("");

		personObject.setRoles(List.of(
			roleWithName("role1"),
				roleWithVdabRoleName("role2", true, false),
				roleWithVdabRoleName("role3", true, true),
				roleWithVdabRoleName("role4", false, false),
				roleWithVdabRoleName("role5", false, true),
				roleWithVdabRoleName("role6", false, true)
		));

		assertThat(personObject.getDisplayRole()).isEqualTo("role5, role6");
	}

	@Test
	void getParentGlobalId() {
		PersonObject personObject = new PersonObject();
		personObject.setParentDn(Dn.of("erglobalid=1234,o=vdab"));
		assertThat(personObject.getParentGlobalId()).isEqualTo("1234");
	}

	private RoleObject roleWithName(String roleName) {
		RoleObject roleObject = new RoleObject();
		roleObject.setRoleName(roleName);
		return roleObject;
	}

	private RoleObject roleWithVdabRoleName(String roleName, boolean pending, boolean hasRole) {
		RoleObject roleObject = new RoleObject();
		roleObject.setVdabRoleName(roleName);
		roleObject.setPending(pending);
		roleObject.setHasRole(hasRole);
		return roleObject;
	}
}