package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.enumeration.Category;
import be.vdab.gebruikersbeheer.derden.exception.GebruikerBestaatReedsOpDezeVestigingException;
import be.vdab.gebruikersbeheer.derden.exception.GebruikerCreateFoutException;
import be.vdab.gebruikersbeheer.derden.exception.PersonCreateValidationException;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.RijksRegisternummerIsReedsIngebruikException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import be.vdab.gebruikersbeheer.derden.extern.validator.PersonFormValidator;
import be.vdab.gebruikersbeheer.derden.intern.command.PersonCommand;
import be.vdab.gebruikersbeheer.derden.service.cache.CacheService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonCreateServiceImpl implements PersonCreateService {

	private final PersonFormValidator personFormValidator;
	private final AdminDomainService adminDomainService;
	private final ApplicationProperties applicationProperties;
	private final PersonService personService;
	private final RoleService roleService;
	private final CacheService cacheService;

	@Override
	public PersonObject create(@NotNull String vestigingGlobalId,
	                           @NotNull PersonObject person,
	                           @NotNull List<RoleObject> rolesToAdd) throws PersonCreateValidationException,
			VestigingNietGevondenException,
			RijksRegisternummerIsReedsIngebruikException,
			GebruikerBestaatReedsOpDezeVestigingException,
			GebruikerCreateFoutException {

		var adminDomainObject = this.adminDomainService.findAdminDomainByDnWithRoles(this.applicationProperties.createAdminDomainDn(vestigingGlobalId))
				.orElseThrow(() -> new VestigingNietGevondenException(vestigingGlobalId));

		valideerInhoudPerson(person, adminDomainObject);

		var adminDomainDn = adminDomainObject.getDn();

		valideerOfGebruikerKanToegevoegdWorden(person, adminDomainDn, adminDomainObject);

		person.setParentDn(adminDomainDn);
		person.setUserId(StringUtils.trimToEmpty(null));
		person.setProfileName(Category.VDABDerde.getType());

		try {
			var filteredRolesToAdd = addAdministratorRolIfFirstUser(adminDomainObject, rolesToAdd);

			// Als er geen rollen toegekend worden, mag de gebruiker aan cache toegevoegd worden, anders gebeurt dit reeds bij toekennen rollen
			var addToCache = filteredRolesToAdd.isEmpty();
			var personDn = personService.insertPerson(adminDomainObject, person, addToCache);
			var insertedPerson = personService.findPersonByDn(personDn, adminDomainObject).orElseThrow(PersonNotFoundException::new);

			// volgende call enkel uitvoeren indien er rollen zijn toegekend
			if (!filteredRolesToAdd.isEmpty()) {
				addRoles(insertedPerson, filteredRolesToAdd, adminDomainObject);
			} else {
				log.debug("geen rollen toe te kennen voor {}", person.getUserId());
			}
			return insertedPerson;
		} catch (Exception e) {
			log.error("Fout bij het creëren van een persoon: {}", e.getMessage());
			throw new GebruikerCreateFoutException(adminDomainObject);
		}
	}

	private List<RoleObject> addAdministratorRolIfFirstUser(AdminDomainObject adminDomainObject, List<RoleObject> rolesToAdd) {
		// not immutable list, DOMAIN ADMINS can be added if necessary
		var filteredRolesToAdd = rolesToAdd.stream()
				.filter(RoleObject::getHasRole)
				.collect(Collectors.toList());

		if (personService.findPersonsFromOrganization(adminDomainObject.getDn()).isEmpty()) {
			// first user
			RoleObject adminRole = roleService.findAdminRole();
			boolean hasAdminRole = filteredRolesToAdd.stream().anyMatch(role -> role.getDn().equals(adminRole.getDn()));

			if (!hasAdminRole) {
				RoleObject adminRoleUser = new RoleObject(adminRole);
				adminRoleUser.setChanged(true);
				adminRoleUser.setHasRole(true);

				filteredRolesToAdd.add(adminRoleUser);
			}
		}

		return filteredRolesToAdd;
	}

	private void valideerInhoudPerson(PersonObject person, AdminDomainObject adminDomainObject) throws PersonCreateValidationException {
		var commandWrapperObject = new PersonCommand(); //nodig omdat velden in validator beginnen met person.
		commandWrapperObject.setPerson(person);
		var validationResult = new PersonCreateValidationException(commandWrapperObject, adminDomainObject);
		personFormValidator.validate(person, validationResult);
		if (validationResult.hasErrors()) {
			throw validationResult;
		}
	}

	private void valideerOfGebruikerKanToegevoegdWorden(PersonObject person, Dn adminDomainDn, AdminDomainObject adminDomainObject) throws RijksRegisternummerIsReedsIngebruikException, GebruikerBestaatReedsOpDezeVestigingException {
		if (!person.isNoRrn()) {
			// enkel controle op rrn als dit ingevuld is. (niet als het via sequence is)
			if (personService.rrnExists(person.getNationalNumber(), adminDomainDn)) {
				throw new RijksRegisternummerIsReedsIngebruikException(adminDomainObject);
			}

			final var existingPersonDn = personService.getPersonDnInPrullenbakForRrnAndIkpNummer(person.getNationalNumber(), adminDomainObject.getIkp());
			if (existingPersonDn != null) {
				throw new GebruikerBestaatReedsOpDezeVestigingException(adminDomainObject);
			}
		}
	}

	private void addRoles(PersonObject insertedPerson, List<RoleObject> filteredRolesToAdd, AdminDomainObject adminDomainObject) {
		insertedPerson.setRoles(filteredRolesToAdd);

		roleService.addAndRemoveRoles(insertedPerson, adminDomainObject);

		if (!waitForRoles(insertedPerson.getDn(), filteredRolesToAdd, adminDomainObject)) {
			log.debug("Alle rollen voor {} zijn nog niet toegekend", insertedPerson.getFullName());

			personService.updatePersonCaches(insertedPerson.getDn(), adminDomainObject);
		}
	}

	private boolean waitForRoles(Dn personDn, List<RoleObject> rolesToAdd, AdminDomainObject adminDomainObject) {
		var counter = 0;
		var delay = 10;

		while (counter < delay) {
			var personObjectNew = personService.findPersonByDn(personDn, adminDomainObject).orElse(null);

			if (personObjectNew == null) {
				break;
			}

			if (personObjectNew.getRoles() != null) {
				var addedRoles = personObjectNew.getRoles().stream().filter(RoleObject::getHasRole).map(RoleObject::getDn).collect(Collectors.toSet());
				if (!addedRoles.isEmpty()) {
					// controle of alle rollen zijn toegevoegd.
					var rolesToAddZonderCVS = rolesToAdd.stream()
							.filter(r -> !"CVS".equals(r.getRoleName())).map(RoleObject::getDn)
							.toList();
					if (!rolesToAddZonderCVS.isEmpty()) {
						boolean alleRollenToegekend = true;
						for (var rol : rolesToAddZonderCVS) {
							if (!addedRoles.contains(rol)) {
								if (log.isTraceEnabled()) {
									log.trace("Person {} has not yet rol {}", personDn, rol);
								}

								alleRollenToegekend = false;
								break;
							}
						}

						if (alleRollenToegekend) {
							cacheService.deleteFromPersonCaches(personDn);
							return true;
						}
					}
				}
			} else {
				log.debug("person {} nog geen rollen", personObjectNew.getFullName());
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}

			counter++;
		}

		cacheService.deleteFromPersonCaches(personDn);

		return false;
	}
}
