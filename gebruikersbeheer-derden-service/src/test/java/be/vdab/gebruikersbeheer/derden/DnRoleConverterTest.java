package be.vdab.gebruikersbeheer.derden;

import be.vdab.gebruikersbeheer.derden.converter.DnToRoleConverter;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DnRoleConverterTest {
	@Test
	void convert() {
		Dn dn = new Dn("erglobalid=1234567890,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,O=VDAB,C=BE");
		Dn roleDn = new Dn("erglobalid=1234567891,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,O=VDAB,C=BE");
		RoleObject roleObject = new RoleObject();
		roleObject.setDn(roleDn);
		DnToRoleConverter dnToRoleConverter = new DnToRoleConverter();
		Map<String, RoleObject> roleObjects = new HashMap<>();
		roleObjects.put("1234567890", roleObject);
		Map<RoleObject, List<RoleObject>> technicalRoleMapping = new HashMap<>();
		technicalRoleMapping.put(roleObject, Collections.singletonList(roleObject));


		List<RoleObject> roles = dnToRoleConverter.convert(Collections.singleton(dn), roleObjects, technicalRoleMapping);

		assertThat(roles).hasSize(1);
	}
}
