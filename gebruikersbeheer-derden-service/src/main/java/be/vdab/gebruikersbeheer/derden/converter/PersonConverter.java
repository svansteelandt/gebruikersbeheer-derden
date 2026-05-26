package be.vdab.gebruikersbeheer.derden.converter;

import be.vdab.gebruikersbeheer.derden.domain.LoginMethod;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonStatus;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.domain.IsimPerson;
import be.vdab.gebruikersbeheer.util.repository.TaskDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class PersonConverter extends BaseConverter implements Converter<IsimPerson, PersonObject> {
	private final DnToRoleConverter dnToRoleConverter;
	private final TaskDao taskDao;

	public PersonConverter(RoleService roleService, DnToRoleConverter dnToRoleConverter, TaskDao taskDao) {
		super(roleService);
		this.dnToRoleConverter = dnToRoleConverter;
		this.taskDao = taskDao;
	}

	// TODO Hazelcast
	private final HashMap<String, Optional<RoleObject>> technicalRolesCache = new HashMap<>();

	@NonNull
	public List<PersonObject> convertList(List<? extends IsimPerson> persons, boolean lookUpParentObjects) {
		log.trace("convertList");

		List<PersonObject> listPersonObject = new ArrayList<>();

		if (persons != null) {
			for (IsimPerson person : persons) {
				PersonObject personObject = convert(person, lookUpParentObjects);
				listPersonObject.add(personObject);
			}
		}

		return listPersonObject;
	}

	public List<PersonObject> convertList(List<? extends IsimPerson> persons, Map<String, RoleObject> roleObjects) {
		return convertList(persons, roleObjects, true);
	}

	public List<PersonObject> convertList(List<? extends IsimPerson> persons, Map<String, RoleObject> roleObjects, boolean lookUpObjects) {
		log.trace("convertList");

		if (persons == null) {
			persons = new ArrayList<>();
		}

		HashMap<RoleObject, List<RoleObject>> technicalRoleMapping = new HashMap<>(roleObjects.size());
		for (RoleObject role : roleObjects.values()) {
			// zoek achter 'technische roles' voor 'pending' echte rollen
			Optional<RoleObject> prereqRole;
			if (technicalRolesCache.containsKey(role.getRoleName() + "_prerequisite")) {
				prereqRole = technicalRolesCache.get(role.getRoleName() + "_prerequisite");
			} else {
				prereqRole = roleService.findByRoleName(role.getRoleName() + "_prerequisite");
				technicalRolesCache.put(role.getRoleName() + "_prerequisite", prereqRole);
			}

			Optional<RoleObject> allowRole;
			if (technicalRolesCache.containsKey(role.getRoleName() + "_allow")) {
				allowRole = technicalRolesCache.get(role.getRoleName() + "_allow");
			} else {
				allowRole = roleService.findByRoleName(role.getRoleName() + "_allow");
				technicalRolesCache.put(role.getRoleName() + "_allow", prereqRole);
			}

			ArrayList<RoleObject> technicalRoles = new ArrayList<>(2);
			prereqRole.ifPresent(technicalRoles::add);
			allowRole.ifPresent(technicalRoles::add);
			technicalRoleMapping.put(role, technicalRoles);
		}

		List<PersonObject> listPersonObject = new ArrayList<>();
		for (IsimPerson person : persons) {
			PersonObject personObject = convert(person, lookUpObjects);
			Set<Dn> dnRollen = person.getRollen();
			personObject.setRoles(dnToRoleConverter.convert(dnRollen, roleObjects, technicalRoleMapping));

			// pending roles (roles with approval)
			List<String> pendingRoles = this.taskDao.getPendingRoles(personObject.getUserId());
			if (pendingRoles != null) {
				// pending roles via approval task
				roleObjects.values().stream().filter(r -> pendingRoles.contains(r.getRoleName())).forEach(r -> {
					personObject.getRoles().add(r);

					r.setHasRole(true);
					r.setPending(true);
				});
			}

			listPersonObject.add(personObject);
		}

		return listPersonObject;
	}

	@NonNull
	public PersonObject convert(@NonNull IsimPerson person) {
		return convert(person, true);
	}

	@NonNull
	public PersonObject convert(IsimPerson person, boolean lookUpObjects) {
		log.trace("convert {} lookup: {}", person.getDn(), lookUpObjects);

		PersonObject personObject = new PersonObject(person.getDn());
		if (person.isVirtual()) {
			personObject.setProfileName("vdabvirtual");
		} else if (person.isIntern()) {
			personObject.setProfileName("vdabintern");
		} else if (person.isDerde()) {
			personObject.setProfileName("vdabderde");
		}

		personObject.setVirtualAccount(person.isVirtual());

		personObject.setCommonName(person.getCn());
		personObject.setFirstName(person.getVoornaam());
		personObject.setLastName(person.getNaam());
		if (person.getEmployeeNumber() != null) {
			personObject.setEmployeenumber(person.getEmployeeNumber());
		}
		personObject.setEmployeeType(person.getEmployeeType());
		personObject.setEmailAddress(person.getEmail());
		if (person.getRijksregisternummer() != null) {
			personObject.setNationalNumber(person.getRijksregisternummer());
		}
		personObject.setPhone(person.getTelefoon());
		personObject.setMobile(person.getMobile());
		if (person.getIkp() != null) {
			personObject.setIkp(person.getIkp());
		}
		personObject.setBedrijfsnaam(person.getBedrijfsnaam());

		if (person.getVdabOe() != null) {
			personObject.setVdaboe(person.getVdabOe().toString());
		}
		personObject.setVdabCvsRol(person.getCvsrole());

		personObject.setUserId(person.getUid());
		personObject.setVdabUid(person.getVdabUid());
		personObject.setLoginMethod(person.getAuthenticationLevel());
		if (personObject.getLoginMethod() == null) {
			personObject.setLoginMethod(LoginMethod.UP.getValue());
		}
		personObject.setParentDn(person.getParentDn());
		personObject.setSuspend(person.getSuspend());
		personObject.setSuspendOmschrijving(person.getSuspendOmschrijving());
		personObject.setStatus(PersonStatus.valueOf(person.getAccountStatus().name()));
		personObject.setDeleteDescription(person.getDeleteDescription());

		if (lookUpObjects) {
			boolean setHasRole = true;
			personObject.setRoles(getRoles(person.getRollen(), setHasRole));
		}

		return personObject;
	}

	public RoleObject getPrerequisiteRole(String roleName) {
		Optional<RoleObject> optional;

		String prereqRoleName = roleName + "_prerequisite";
		if (technicalRolesCache.containsKey(prereqRoleName)) {
			optional = technicalRolesCache.get(prereqRoleName);

			if (optional.isPresent()) {
				return optional.get();
			}
		}

		return null;
	}
}
