package be.vdab.gebruikersbeheer.derden.converter;

import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class BaseConverter {

	protected final RoleService roleService;

	public List<RoleObject> getRoles(Collection<Dn> roles) {
		return getRoles(roles, false);
	}

	public List<RoleObject> getRoles(Collection<Dn> roles, boolean setHasRole) {
		// ophalen van de rollen
		if (roleService != null && roles != null) {
			long start = System.currentTimeMillis();

			final List<RoleObject> rollen = new ArrayList<>();

			List<Dn> rolesInCache = roles.stream().filter(r -> roleService.isRoleInCache(r)).collect(Collectors.toList());
			List<Dn> rolesNotInCache = roles.stream().filter(r -> !roleService.isRoleInCache(r)).collect(Collectors.toList());

			List<RoleObject> roleObjects = rolesInCache.stream()
					.map(r -> this.roleService.findRoleByDN(r))
					.flatMap(Optional::stream)
					.collect(Collectors.toList());

			if (!rolesNotInCache.isEmpty()) {
				List<String> globalIds = rolesNotInCache.stream()
						.map(Dn::getGlobalId)
						.collect(Collectors.toList());

				List<RoleObject> roleObjectsSearch = this.roleService.findRolesByGlobalId(globalIds);

				if (roleObjectsSearch != null && !roleObjectsSearch.isEmpty()) {
					roleObjects.addAll(roleObjectsSearch);

					roleObjectsSearch.forEach(roleObject -> this.roleService.addRoleToCache(roleObject));
				}
			}

			roleObjects.forEach(roleObject -> {
				if (setHasRole) {
					RoleObject roleObjectCopy = new RoleObject(roleObject);
					roleObjectCopy.setHasRole(true);

					rollen.add(roleObjectCopy);
				} else {
					rollen.add(roleObject);
				}
			});

			if (log.isTraceEnabled()) {
				log.trace("duur getRoles {}  sec.", ((double) System.currentTimeMillis() - start) / 1000);
			}

			return rollen;
		}

		return new ArrayList<>();
	}
}
