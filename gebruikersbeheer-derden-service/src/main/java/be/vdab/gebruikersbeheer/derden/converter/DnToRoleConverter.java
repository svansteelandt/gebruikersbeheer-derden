package be.vdab.gebruikersbeheer.derden.converter;

import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class DnToRoleConverter {

	public List<RoleObject> convert(Set<Dn> userRoleCollection, Map<String, RoleObject> roleObjects, Map<RoleObject, List<RoleObject>> technicalRoleMapping) {
		log.trace("convert {}", userRoleCollection);

		List<RoleObject> listRoleObject = new ArrayList<>();
		List<String> userRoleErGlobalIds = new ArrayList<>();
		for (Dn role : userRoleCollection) {
			voegRolToeIndienUserRolDezeBevat(roleObjects, listRoleObject, userRoleErGlobalIds, role);
		}
		wijsTechnischeRollenToe(technicalRoleMapping, listRoleObject, userRoleErGlobalIds);

		return listRoleObject;
	}

	private void voegRolToeIndienUserRolDezeBevat(Map<String, RoleObject> roleObjects, List<RoleObject> listRoleObject, List<String> userRoleErGlobalIds, Dn role) {
		if (role != null) {
			String roleErGlobalId = role.getGlobalId();
			userRoleErGlobalIds.add(roleErGlobalId);
			RoleObject roleObject = roleObjects.get(roleErGlobalId);
			if (roleObject != null) {
				RoleObject copyForPerson = new RoleObject(roleObject);
				copyForPerson.setHasRole(true);
				listRoleObject.add(copyForPerson);
			}
		}
	}

	private void wijsTechnischeRollenToe(Map<RoleObject, List<RoleObject>> technicalRoleMapping, List<RoleObject> listRoleObject, List<String> userRoleErGlobalIds) {
		for (RoleObject toewijsbareRole : technicalRoleMapping.keySet()) {
			if (!technicalRoleMapping.get(toewijsbareRole).isEmpty()) {
				boolean hasAllTechnicalRoles = true;
				for (RoleObject technicalRol : technicalRoleMapping.get(toewijsbareRole)) {
					if (!userRoleErGlobalIds.contains(technicalRol.getDn().getGlobalId())) {
						hasAllTechnicalRoles = false;
						break;
					}
				}
				if (hasAllTechnicalRoles) {
					RoleObject copyForPerson = new RoleObject(toewijsbareRole);
					copyForPerson.setPending(true);
					listRoleObject.add(copyForPerson);
				}
			}
		}
	}
}
