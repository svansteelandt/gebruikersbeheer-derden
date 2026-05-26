package be.vdab.gebruikersbeheer.derden.converter;

import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.isim.domain.IsimRole;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Component
public class RoleConverter implements Converter<IsimRole, RoleObject> {

	private final IsimLdapManager isimLdapManager;

	public RoleConverter(IsimLdapManager isimLdapManager) {
		this.isimLdapManager = isimLdapManager;
	}

	public RoleObject convert(IsimRole role) {
		RoleObject roleObject = new RoleObject();
		roleObject.setDn(role.getDn());
		roleObject.setRoleName(role.getRoleName());
		roleObject.setRoleDescription(role.getRoleDescription());
		roleObject.setVdabRoleName(role.getVdabRoleName());
		roleObject.setVdabRoleDescription(role.getVdabRoleDescription());
		roleObject.setNeedsApproval(role.isNeedsApproval());
		roleObject.setDynamicRole(role.isDynamicRole());
		roleObject.setTags(role.getTags());
		roleObject.setOwners(role.getOwners());
		roleObject.setAdminRole(RoleNames.ROL_DOMAIN_ADMINS.equalsIgnoreCase(role.getRoleName()));

		return roleObject;
	}

	public List<RoleObject> convertRolesList(Collection<RoleObject> roles, boolean processTechnischeRoles) {
		List<RoleObject> listRoleObject = new ArrayList<>();
		HashMap<String, Integer> pendingRolesHerkenning = new HashMap<>();
		for (RoleObject role : roles) {
			String[] roleNameParts = role.getRoleName().split("_");
			if (roleNameParts.length > 1) {
				if (processTechnischeRoles && "prerequisite".equalsIgnoreCase(roleNameParts[1]) || "allow".equalsIgnoreCase(roleNameParts[1])) {
					//technische roles voor pending
					Integer aantalRollenHerkendVoorRolnaam = pendingRolesHerkenning.get(roleNameParts[0]);
					if (aantalRollenHerkendVoorRolnaam == null) {
						pendingRolesHerkenning.put(roleNameParts[0], 1);
					} else {
						pendingRolesHerkenning.put(roleNameParts[0], 2);
					}
				} else {
					// niet technische roles
					listRoleObject.add(role);
				}
			} else {
				listRoleObject.add(role);
			}
		}

		// pending roles toevoegen adhv herkende technische rollen (aantal moet 2 zijn)
		for (String rolename_prefix : pendingRolesHerkenning.keySet()) {
			if (pendingRolesHerkenning.get(rolename_prefix) == 2) {
				// pending role opzoeken adhv technischerol prefix -> moet naam van rol zijn !
				isimLdapManager.getNonDynamicRoleByName(rolename_prefix)
						.map(this::convert)
						.ifPresent(roleObject -> {
							roleObject.setPending(true);
							listRoleObject.add(roleObject);
						});
			}
		}

		return listRoleObject;
	}
}
