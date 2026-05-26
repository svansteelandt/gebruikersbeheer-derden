package be.vdab.gebruikersbeheer.derden.domain;

import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminDomainObjectTest {
	@Test
	void addAndDeleteAdmin() {
		Dn dn = new Dn("dn");
		PersonObject personObject = new PersonObject();
		personObject.setDn(dn);
		AdminDomainObject adminDomainObject = new AdminDomainObject();
		adminDomainObject.setOu("o");
		assertThat(adminDomainObject.getOu()).isEqualTo("o");

		adminDomainObject.addAdministrator(personObject);
		assertThat(adminDomainObject.getAdministrators()).hasSize(1);

		adminDomainObject.deleteAdministrator(personObject);
		assertThat(adminDomainObject.getAdministrators()).isEmpty();
	}
	@Test
	void toStringMethod() {
		AdminDomainObject adminDomainObject = new AdminDomainObject();
		assertThat(adminDomainObject.toString()).isNotEmpty();
	}
}
