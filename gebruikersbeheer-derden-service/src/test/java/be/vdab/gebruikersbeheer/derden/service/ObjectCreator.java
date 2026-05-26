package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;

import java.util.ArrayList;
import java.util.List;

public class ObjectCreator {
	static PersonObject createPersonObject(String globalId) {
		Dn dnObject = Dn.of("erglobalid=" + globalId + ",ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");
		PersonObject personObject = new PersonObject();
		personObject.setVirtualAccount(false);
		personObject.setDn(dnObject);
		personObject.setNationalNumber("rrnr");
		personObject.setEmailAddress("test@vdab.be");
		return personObject;
	}

	static Dn getDn() {
		return Dn.of("erglobalid=692359561954987775,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");
	}

	private static Dn getDnRole() {
		return Dn.of("erglobalid=7068625278676316999,ou=roles,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");
	}

	static AdminDomainObject createAdminDomainObject(Dn dnObject) {
		AdminDomainObject adminDomainObject = new AdminDomainObject();
		adminDomainObject.setDn(dnObject);

		List<RoleObject> roles = new ArrayList<>();
		RoleObject role1 = new RoleObject();
		role1.setDn(getDnRole());
		role1.setRoleName("VACATURE- EN KANDIDATENBEHEER");
		roles.add(role1);

		adminDomainObject.setRoles(roles);

		return adminDomainObject;
	}
}
