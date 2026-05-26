package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleAssignmentResult;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.PersonRoleAssignmentNotYetFinishedException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.util.LogSanitizer;
import be.vdab.gebruikersbeheer.derden.util.ValidateUtils;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonChangeRolesServiceImpl implements PersonChangeRolesService {

	private final AdminDomainService adminDomainService;
	private final AdminService adminService;
	private final ApplicationProperties applicationProperties;
	private final IsimUserContextManager isimUserContextManager;
	private final IsimLdapManager isimLdapManager;
	private final PersonService personService;
	private final RoleService roleService;
	private final RetryTemplate retryTemplate;
	private final ValidateUtils validateUtils;

	@Override
	public RoleAssignmentResult changeRoles(@NotNull String personGlobalId, @NotNull List<String> roleIds, String csvRole) {
		var personObject = personService.findPersonWithRolesByGlobalId(personGlobalId).orElseThrow(() -> new PersonNotFoundException(personGlobalId));
		var adminDomainObject = this.adminDomainService.findAdminDomainByDnWithRoles(personObject.getParentDn()).orElseThrow(() -> new VestigingNietGevondenException(personObject.getParentDn().getGlobalId()));
		updateRolesWhichAreChanged(roleIds, personObject);

		var result = executeRoleChange(personObject, personObject.getRoles(), csvRole, adminDomainObject);

		if (result.someRolesAreChanged()) {
			waitForRoleUpdateToComplete(personGlobalId, roleIds, personObject);
		}

		updateCache(adminDomainObject, personObject);

		return result;
	}

	private static void updateRolesWhichAreChanged(List<String> roleIds, PersonObject personObject) {
		personObject.getRoles().forEach(role -> {
			if (role.getHasRole() && !roleIds.contains(role.getGlobalId())) {
				role.setChanged(true);
				role.setHasRole(false);
			} else if (!role.getHasRole() && roleIds.contains(role.getGlobalId())) {
				role.setChanged(true);
				role.setHasRole(true);
			}
		});
	}

	private void updateCache(AdminDomainObject adminDomainObject, PersonObject personObject) {
		personService.updatePersonCaches(personObject.getDn(), adminDomainObject);
	}

	private void waitForRoleUpdateToComplete(String personGlobalId, List<String> roleIds, PersonObject personObject) {
		retryTemplate.execute(retryContext -> {
			log.debug("Retring update person {} to get all new roles: {}", personObject.getDn().getGlobalId(), retryContext.getRetryCount());
			var updatedPersonObject = personService.findPersonWithRolesByGlobalId(personGlobalId).orElseThrow(() -> new PersonNotFoundException(personGlobalId));
			if (updatedPersonObject.getRoles().stream().filter(RoleObject::getHasRole).map(RoleObject::getGlobalId).toList().size() != roleIds.size()) {
				throw new PersonRoleAssignmentNotYetFinishedException();
			}
			return null;
		});
	}

	private RoleAssignmentResult executeRoleChange(@NonNull PersonObject personToChange,
	                                               @NonNull List<RoleObject> rolesToChange,
	                                               String cvsRole,
	                                               AdminDomainObject organization) {
		var cvsRoleChanged = false;
		var isimWsSession = isimUserContextManager.getSession();
		var personOld = isimLdapManager.getPersonByDn(personToChange.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN)
				.orElseThrow(PersonNotFoundException::new);

		ArrayList<RoleObject> addRoles = new ArrayList<>();
		ArrayList<RoleObject> deleteRoles = new ArrayList<>();

		if (cvsRole != null && !cvsRole.equals(personToChange.getVdabCvsRol())) {
			personToChange.setVdabCvsRol(cvsRole);
			cvsRoleChanged = true;
		}

		var result = handleRoleChanges(personToChange, rolesToChange, organization, personOld, addRoles, deleteRoles);
		var rollenMoetenToegevoegdOfGewijzigdWorden = !addRoles.isEmpty() || !deleteRoles.isEmpty() || cvsRoleChanged;
		var cvsRolMoetToegevoegdWorden = moetCvsRolWordenToegevoegd(result.cvsRoleObject(), personOld);
		if (rollenMoetenToegevoegdOfGewijzigdWorden) {
			roleService.changePersonRoles(isimWsSession, personOld, personToChange, addRoles, deleteRoles);
		}
		if (cvsRolMoetToegevoegdWorden) {
			roleService.changePersonRole(isimWsSession, personToChange, result.cvsRoleObject(), true);
		}
		return new RoleAssignmentResult(rollenMoetenToegevoegdOfGewijzigdWorden || cvsRolMoetToegevoegdWorden, result.minAdminsReached(), result.maxAdminsReached());
	}

	private RoleChangesResult handleRoleChanges(PersonObject personToChange,
	                                            List<RoleObject> rolesToChange,
	                                            AdminDomainObject organization,
	                                            IsimLdapPerson personOld,
	                                            ArrayList<RoleObject> addRoles,
	                                            ArrayList<RoleObject> deleteRoles) {
		var nbrOfAdminsUnderOrganization = roleService.getAantalAdmins(organization);
		boolean maxAdminsReached = false;
		boolean minAdminsReached = false;
		RoleObject cvsRole = null;

		for (var roleObject : rolesToChange) {
			if (roleObject.isChanged()) {
				if (roleObject.isAdminRole()) {
					var result = handleAdminRoleChange(personToChange, roleObject, organization, personOld, nbrOfAdminsUnderOrganization, addRoles, deleteRoles);
					maxAdminsReached = maxAdminsReached || result.maxAdminsReached();
					minAdminsReached = minAdminsReached || result.minAdminsReached;
				} else {
					var result = handleRoleChange(personToChange, roleObject, personOld, addRoles, deleteRoles);
					cvsRole = cvsRole != null ? cvsRole : result;
				}
			}
		}
		return new RoleChangesResult(minAdminsReached, maxAdminsReached, cvsRole);
	}

	private AdminRoleChangeResult handleAdminRoleChange(PersonObject personToChange,
	                                                    RoleObject roleObject,
	                                                    AdminDomainObject organization,
	                                                    IsimLdapPerson personOld,
	                                                    int nbrOfAdminsUnderOrganization,
	                                                    List<RoleObject> addRoles,
	                                                    List<RoleObject> deleteRoles) {
		var minAdminsReached = false;
		var maxAdminsReached = false;
		if (roleObject.getHasRole()) {
			maxAdminsReached = behandelAdminRolDieGebruikerKrijgt(personToChange, organization, roleObject, personOld, nbrOfAdminsUnderOrganization, addRoles);
		} else {
			minAdminsReached = behandelAdminRolDieGebruikerVerliest(personToChange, organization, roleObject, nbrOfAdminsUnderOrganization, deleteRoles);
		}
		return new AdminRoleChangeResult(minAdminsReached, maxAdminsReached);
	}

	private RoleObject handleRoleChange(PersonObject personToChange,
	                                    RoleObject roleObject,
	                                    IsimLdapPerson personOld,
	                                    List<RoleObject> addRoles,
	                                    List<RoleObject> deleteRoles) {
		if (roleObject.getHasRole()) {
			return behandelRolDieGebruikerKrijgt(personToChange, roleObject, personOld, addRoles);
		} else {
			deleteRoles.add(roleObject);
			return null;
		}
	}


	private RoleObject behandelRolDieGebruikerKrijgt(PersonObject personToChange, RoleObject roleObject, IsimLdapPerson personOld, List<RoleObject> addRoles) {
		RoleObject cvsRoleObject = null;
		if (roleService.personHasRole(personOld, roleObject)) {
			log.debug("Person {} had reeds rol {} -> niets doen", LogSanitizer.sanitize(personToChange.getUserId()), LogSanitizer.sanitize(roleObject.getVdabRoleName()));
		} else {
			if (this.validateUtils.isCVSRole(roleObject)) {
				if (!roleObject.isPending()) {
					// CVS rol apart submitten indien niet pending (anders dubbel)
					cvsRoleObject = roleObject;
				}
			} else {
				addRoles.add(roleObject);
			}
		}
		return cvsRoleObject;
	}

	private boolean behandelAdminRolDieGebruikerVerliest(PersonObject personToChange, AdminDomainObject organization, RoleObject roleObject, int nbrOfAdminsUnderOrganization, List<RoleObject> deleteRoles) {
		boolean minAdminsReached = false;
		if (this.validateUtils.minAdminsReached(nbrOfAdminsUnderOrganization)) {
			minAdminsReached = true;
		} else {
			adminService.deleteAdmin(organization, personToChange);
			deleteRoles.add(roleObject);
		}
		return minAdminsReached;
	}

	private boolean behandelAdminRolDieGebruikerKrijgt(PersonObject personToChange, AdminDomainObject organization, RoleObject roleObject, IsimLdapPerson personOld, int nbrOfAdminsUnderOrganization, List<RoleObject> addRoles) {
		boolean maxAdminsReached = false;

		if (roleService.personHasRole(personOld, roleObject)) {
			log.debug("Person {} had reeds rol {} -> niets doen", LogSanitizer.sanitize(personToChange.getUserId()), LogSanitizer.sanitize(roleObject.getVdabRoleName()));
		} else {
			if (this.validateUtils.maxAdminsReached(nbrOfAdminsUnderOrganization)) {
				maxAdminsReached = true;
			} else {
				adminService.createNewAdmin(organization, personToChange);
				addRoles.add(roleObject);
			}
		}
		return maxAdminsReached;
	}

	private static boolean moetCvsRolWordenToegevoegd(RoleObject cvsRoleObject, IsimLdapPerson personOld) {
		var cvsRoleToevoegen = false;

		if (cvsRoleObject != null) {
			var dnCvsRol = cvsRoleObject.getDn();
			var rolesPerson = personOld.getRollen();
			var listCVSRole = rolesPerson.stream().filter(dnCvsRol::equals).toList();

			if (listCVSRole.size() != 1) {
				cvsRoleToevoegen = true;
			} else {
				log.trace("person {} has already CVS Role", personOld.getDn());
			}
		}
		return cvsRoleToevoegen;
	}

	private record RoleChangesResult(boolean minAdminsReached, boolean maxAdminsReached, RoleObject cvsRoleObject) {
	}

	private record AdminRoleChangeResult(boolean minAdminsReached, boolean maxAdminsReached) {
	}

}
