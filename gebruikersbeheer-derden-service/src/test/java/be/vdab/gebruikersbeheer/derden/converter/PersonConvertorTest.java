package be.vdab.gebruikersbeheer.derden.converter;

import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.ldap.LdapAbstraction;
import be.vdab.gebruikersbeheer.util.isim.domain.IsimPerson;
import be.vdab.gebruikersbeheer.util.isim.domain.AccountStatus;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import be.vdab.gebruikersbeheer.util.repository.TaskDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ldap.core.LdapAttributes;

import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonConvertorTest {
	@InjectMocks
	PersonConverter personConverter;

	@Mock
	DnToRoleConverter dnToRoleConverter;

	@Mock
	TaskDao taskDao;

	@Mock
	IsimLdapPerson isimPerson;

	@Test
	void convertListForPersonWithoutRoles() {
		when(isimPerson.getUid()).thenReturn("test,intern");
		when(isimPerson.getAccountStatus()).thenReturn(AccountStatus.ACTIVE);
		List<IsimLdapPerson> ldapPersons = new ArrayList<>();
		ldapPersons.add(isimPerson);
		HashMap<String, RoleObject> roleObjectHashMap = new HashMap<>();

		assertThat(personConverter.convertList(ldapPersons, roleObjectHashMap)).isNotNull();
		verify(taskDao).getPendingRoles("test,intern");
	}

	@Test
	void convertGetProfileName() {
		Attributes attrs = new LdapAttributes();
		attrs.put("objectclass", "vdabintern");

		LdapAbstraction abstraction = new LdapAbstraction(null, attrs);
		IsimPerson isimPerson = new IsimLdapPerson(abstraction);

		PersonObject personObject = personConverter.convert(isimPerson);

		assertThat(personObject.getProfileName()).isEqualTo("vdabintern");
	}

	@Test
	void convertGetProfileName2() {
		Attributes attrs = new LdapAttributes();
		attrs.put("objectclass", "vdabderde");

		LdapAbstraction abstraction = new LdapAbstraction(null, attrs);
		IsimPerson isimPerson = new IsimLdapPerson(abstraction);

		PersonObject personObject = personConverter.convert(isimPerson);

		assertThat(personObject.getProfileName()).isEqualTo("vdabderde");
	}
}
