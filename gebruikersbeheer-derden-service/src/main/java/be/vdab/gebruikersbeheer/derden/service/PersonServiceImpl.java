package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.config.CacheNames;
import be.vdab.gebruikersbeheer.derden.converter.PersonConverter;
import be.vdab.gebruikersbeheer.derden.converter.PersonDeconverter;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.LoginMethod;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.RoleNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.service.cache.CacheService;
import be.vdab.gebruikersbeheer.derden.util.LogSanitizer;
import be.vdab.gebruikersbeheer.derden.util.ValidateUtils;
import be.vdab.gebruikersbeheer.derden.web.internal.v1.Insz;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.exception.IsimApplicationException;
import be.vdab.gebruikersbeheer.util.exception.IsimRuntimeException;
import be.vdab.gebruikersbeheer.util.exception.OrganizationNotFoundException;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsPerson;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsRequest;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsRequestService;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimObjectClasses;
import be.vdab.gebruikersbeheer.util.isim.domain.WorkflowProcess;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.AndFilterBuilder;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.LdapFilter;
import be.vdab.gebruikersbeheer.util.isim.service.IsimRoleService;
import be.vdab.gebruikersbeheer.util.service.BusinessWebservice;
import be.vdab.gebruikersbeheer.util.service.CreatePersonRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

	private static final String NO_PERSON_FOUND_WITH_INSZ_FOR_ORG_ID = "No person found with INSZ: %s for OrgId: %s";
	private static final int SECONDS_TO_WAIT_FOR_CREATION = 120;

	private final IsimLdapManager isimLdapManager;
	private final IsimWsClient isimWsClient;
	private final IsimWsRequestService isimWsRequestService;
	private final IsimRoleService isimRoleService;
	private final PersonConverter personConverter;
	private final PersonDeconverter personDeconverter;
	private final ValidateUtils validateUtils;
	private final AdminDomainService adminDomainService;
	private final AdminService adminService;
	private final TaskService taskService;
	private final RoleService roleService;
	private final ApplicationProperties applicationProperties;
	private final BusinessWebservice businessWebservice;
	private final PersonOrganizationService personOrganizationService;
	private final PersonCreator personCreator;
	private final CacheService cacheService;
	private final IsimUserContextManager isimUserContextManager;

	@Override
	public List<Dn> findPersonsFromOrganization(Dn organizationDn) {
		return isimLdapManager.getPersonDnsForAdminDomain(organizationDn);
	}

	/**
	 * Finds persons from organization&#46; <br/> the technical roles will be processed and hidden for the persons
	 *
	 * @return List<PersonObject> <code>list</code>
	 */
	@Override
	public List<PersonObject> findPersonsFromOrganization(AdminDomainObject adminDomain) {
		log.trace("findPersonsFromOrganization({})", adminDomain.getName());

		List<IsimLdapPerson> personList = isimLdapManager.getPersonsForAdminDomain(adminDomain.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN);
		Map<String, RoleObject> roles = getAdminDomainRolesAsMap(adminDomain);
		return personConverter.convertList(personList, roles, false);
	}

	/**
	 * Finds persons from organization&#46; <br/> the technical roles will be processed and hidden for the persons
	 *
	 * @return List<PersonObject> <code>list</code>
	 */
	@Override
	public List<PersonObject> findNonMlpOpleidingPersonsFromOrganization(AdminDomainObject adminDomain) {
		log.trace("findPersonsFromOrganization({})", adminDomain.getName());

		LdapFilter notMlpOpleidingFilter = LdapFilter.notFilter(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_EMPLOYEETYPE, "MlpOpleiding"));
		List<IsimLdapPerson> personList = isimLdapManager.getPersonsForAdminDomain(adminDomain.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN, notMlpOpleidingFilter);
		Map<String, RoleObject> roles = getAdminDomainRolesAsMap(adminDomain);
		return personConverter.convertList(personList, roles, false);
	}

	private static Map<String, RoleObject> getAdminDomainRolesAsMap(AdminDomainObject adminDomain) {
		return adminDomain.getRoles().stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(roleObject -> roleObject.getDn().getGlobalId(), roleObject -> roleObject));
	}

	@Override
	public Dn insertPerson(AdminDomainObject adminDomainObject, PersonObject personObject, boolean addPersonToCache) {
		String rijksregisterNummer = personObject.getNationalNumber();
		if (personObject.isNoRrn()) {
			rijksregisterNummer = null;
			personObject.setLoginMethod(LoginMethod.OTP.getValue());
		} else {
			personObject.setLoginMethod(getLoginMethodNewUser(personObject.getNationalNumber()).getValue());
		}

		CreatePersonRequest createPersonRequest = CreatePersonRequest.aCreatePersonRequest()
				.voornaam(personObject.getFirstName())
				.naam(personObject.getLastName())
				.insz(rijksregisterNummer)
				.email(personObject.getEmailAddress())
				.build();
		Long personId = this.businessWebservice.createPerson(createPersonRequest);
		if (personId == null) {
			log.error("insertPerson GebruikersbeheerWebservice werkt NIET!");
			throw new IsimApplicationException("Fout bij het creëren van een gebruiker (Webservice werkt niet).");
		}

		log.debug("insertPerson {} gebruikersbeheerWebservice --> {}", LogSanitizer.sanitize(personObject.toString()), personId);

		personObject.setEmployeenumber(personId);

		try {
			Optional<Dn> personDn = personCreator.createPersonInIsim(adminDomainObject, personObject);

			if (addPersonToCache) {
				personDn.ifPresent(dn -> insertPersonInCaches(dn, adminDomainObject));
			}

			return personDn.orElseThrow(() -> new IsimApplicationException("Fout bij opvragen person na creatie in ISIM"));
		} catch (Exception e) {
			log.error("{}, wait {} seconds.", e.getMessage(), SECONDS_TO_WAIT_FOR_CREATION);
			throw e;
		}
	}

	private boolean hasProfileWithOtpOrAcm(List<PersonObject> derdeProfiles) {
		return derdeProfiles.stream().anyMatch(po ->
				po.getLoginMethod().equals(LoginMethod.ACM.getValue()) ||
						po.getLoginMethod().equals(LoginMethod.OTP.getValue()));
	}

	private LoginMethod getLoginMethodNewUser(String insz) {
		List<PersonObject> derdeProfiles = getDerdeProfilesBy(insz);
		if (derdeProfiles.isEmpty()) {
			return LoginMethod.OTP;
		}

		return hasProfileWithOtpOrAcm(derdeProfiles) ? LoginMethod.OTP : LoginMethod.UP;
	}

	/**
	 * Updates person&#46; <br>
	 *
	 * @param personObjectNew person object to update
	 */
	@Override
	public void updatePerson(PersonObject personObjectNew) {
		log.trace("updatePerson PersonObject: {}", LogSanitizer.sanitize(personObjectNew.toString()));

		IsimWsSession isimWsSession = isimUserContextManager.getSession();
		IsimWsPerson personOld = isimWsClient.getPersonByDn(isimWsSession, personObjectNew.getDn());
		IsimWsPerson personNew = personOld.update(builder -> personDeconverter.convert(personObjectNew, builder));
		isimWsClient.updatePerson(isimWsSession, personOld, personNew)
				.ifPresent(request -> isimWsRequestService.waitUntilFinished(isimWsSession, request));

		cacheService.deleteFromPersonCaches(personObjectNew.getDn());
	}

	/**
	 * Deletes person&#46; <br>
	 *
	 * @param personObject person object to update
	 */
	@Override
	public boolean deletePerson(AdminDomainObject adminDomainObject, PersonObject personObject) throws MinimumAdminsReachedException {
		log.trace("deletePerson PersonObject: '{}' dn: {}", personObject.getCommonName(), personObject.getDn());


		boolean isAdmin = personObject.getRoles().stream().anyMatch(role -> role.isAdminRole() && role.getHasRole());
		if (isAdmin && this.validateUtils.minAdminsReached(roleService.getAantalAdmins(adminDomainObject))) {
			throw new MinimumAdminsReachedException(applicationProperties.getMinDomainAdmins());
		}


		Dn parentDn = personObject.getParentDn();
		if (parentDn != null && isAdmin) {
			if (log.isDebugEnabled()) {
				log.debug("admin domain person: {}", parentDn);
			}
			adminService.deleteAdmin(adminDomainObject, personObject);
		}

		IsimWsSession session = isimUserContextManager.getSession();
		removeWorkFlowProcessesForUser(session, personObject.getDn(), "User is deleted");

		isimWsClient.deletePerson(session, personObject.getDn())
				.ifPresent(request -> isimWsRequestService.waitUntilFinished(session, request));

		this.cacheService.deleteFromPersonCaches(personObject);

		long start = System.currentTimeMillis();
		boolean deleted = Waiter.waitFor(start, SECONDS_TO_WAIT_FOR_CREATION, () -> !personOrganizationService.existsPersonInOrganization(personObject.getDn(), adminDomainObject.getDn()));
		log.debug("wait {} sec. for LDAP sync, result = {}", (((double) System.currentTimeMillis()) - start) / 1000, deleted);

		return deleted;
	}

	@Override
	public boolean restorePerson(PersonObject personObject) {
		IsimWsSession isimWsSession = isimUserContextManager.getSession();
		boolean requestWasSuccessful = isimWsClient.restorePerson(isimWsSession, personObject.getDn())
				.map(request -> isimWsRequestService.waitUntilFinished(isimWsSession, request))
				.map(IsimWsRequest::hasCompletedWithoutFailure)
				.orElse(false);

		if (requestWasSuccessful) {
			// delete from cache
			this.cacheService.deleteFromPersonCaches(personObject);
		}

		return requestWasSuccessful;
	}

	@Override
	public List<PersonObject> findPersonsFromOrganizationDnWithRoleWithPending(Dn erParentDn, RoleObject roleObject) {
		log.trace("findPersonsFromOrganizationDnWithRoleWithPending dn: {} RoleObject: {}", erParentDn, roleObject);

		HashMap<String, RoleObject> roles = new HashMap<>();
		roles.put(roleObject.getDn().getGlobalId(), roleObject);

		List<IsimLdapPerson> persons = isimLdapManager.getPersonsForAdminDomain(erParentDn, DEFAULT_PERSON_ATTRIBUTES_DERDEN);
		List<PersonObject> personList = personConverter.convertList(persons, roles);

		for (PersonObject personObject : personList) {
			int indexOfRole = personObject.getRoles().indexOf(roleObject);
			personObject.setHasRole(indexOfRole != -1);
			personObject.setChanged(false);
			if (indexOfRole != -1) {
				personObject.setPending(personObject.getRoles().get(indexOfRole).isPending());
			}
		}

		return personList;
	}

	@Override
	public String getGlobalIdFromPersonBy(String orgId, Insz insz) {
		Dn orgDn = getOrganizationDn(orgId);
		return getPersonFromOrganization(insz, orgDn)
				.map(personConverter::convert)
				.map(PersonObject::getDn)
				.map(Dn::getGlobalId)
				.orElseThrow(() -> new PersonNotFoundException(NO_PERSON_FOUND_WITH_INSZ_FOR_ORG_ID.formatted(insz.getInszNummer(), orgId)));
	}

	private Optional<IsimLdapPerson> getPersonFromOrganization(Insz insz, Dn orgDn) {
		return isimLdapManager.getPersonsForAdminDomain(orgDn, DEFAULT_PERSON_ATTRIBUTES_DERDEN).stream()
				.filter(person -> person.getRijksregisternummer().equals(insz.getInszNummer()))
				.findFirst();
	}

	private Dn getOrganizationDn(String orgId) {
		return adminDomainService.findAdminDomainByOrgId(orgId)
				.map(AdminDomainObject::getDn)
				.orElseThrow(() -> new OrganizationNotFoundException(orgId));
	}

	@Override
	@Cacheable(cacheNames = CacheNames.CACHE_PERSONS, keyGenerator = "personKeyGenerator")
	public Optional<PersonObject> findPersonByDn(Dn dn, AdminDomainObject adminDomainObject) {
		if (log.isTraceEnabled()) {
			log.trace("findPersonByDn dn: {}", dn);
		}

		Optional<PersonObject> result = isimLdapManager.getPersonByDn(dn, DEFAULT_PERSON_ATTRIBUTES_DERDEN).map(personConverter::convert);
		result.ifPresent(personObject -> {
			AdminDomainObject theAdminDomain = adminDomainObject;
			if (theAdminDomain == null && "vdabintern".equals(personObject.getProfileName())) {
				theAdminDomain = adminDomainService.findAdminDomainByDnWithRoles(personObject.getParentDn()).orElse(null);
			}
			if (adminDomainObject != null) {
				personObject.setRoles(getRolesWithPending(personObject, theAdminDomain));
			}
		});

		return result;
	}

	@Override
	public Optional<PersonObject> findPersonWithRolesByGlobalId(String personGlobalId) {
		var personDn = applicationProperties.createPersonDn(personGlobalId);
		Optional<PersonObject> result = isimLdapManager.getPersonByDn(personDn, DEFAULT_PERSON_ATTRIBUTES_DERDEN).map(personConverter::convert);
		result.ifPresent(personObject -> {
			var theAdminDomain = adminDomainService.findAdminDomainByDnWithRoles(personObject.getParentDn()).orElseThrow(() -> new VestigingNietGevondenException(personObject.getParentDn().getGlobalId()));
			personObject.setRoles(getRolesWithPending(personObject, theAdminDomain));
		});
		return result;
	}

	@Override
	public PersonObject findPersonByGebruikersnaam(String gebruikersnaam) throws PersonNotFoundException {
		return findPersonByGebruikersnaam(gebruikersnaam, DEFAULT_PERSON_ATTRIBUTES_DERDEN);
	}

	@Override
	public PersonObject findPersonByGebruikersnaam(String gebruikersnaam, List<String> attributes) throws PersonNotFoundException {
		return isimLdapManager.getPersonByUid(gebruikersnaam, attributes)
				.map(person -> this.personConverter.convert(person, false))
				.orElseThrow(PersonNotFoundException::new);
	}

	@Override
	public List<PersonObject> findPersons(FindPersonQuery query) {
		List<String> attrList = getAttrList();

		AndFilterBuilder filterBuilder = LdapFilter.andFilter();
		filterBuilder.add(LdapFilter.objectClassFilter(IsimObjectClasses.VDAB_DERDE));

		if (query.gebruikersnaam() != null && !"".equalsIgnoreCase(query.gebruikersnaam())) {
			filterBuilder.add(LdapFilter.orFilter(
					LdapFilter.equalityFilter(IsimAttributeNames.ATTR_UID, LdapEncoder.filterEncode(query.gebruikersnaam()) + "*"),
					LdapFilter.equalityFilter(IsimAttributeNames.ATTR_VDABUID, LdapEncoder.filterEncode(query.gebruikersnaam()) + "*")
			));
		}
		if (isNotEmpty(query.rijksregisternummer())) {
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_RIJKSREGISTERNUMMER, LdapEncoder.filterEncode(query.rijksregisternummer())));
		}
		if (isNotEmpty(query.voornaam())) {
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_GIVENNAME, LdapEncoder.filterEncode(query.voornaam()) + "*"));
		}
		if (isNotEmpty(query.naam())) {
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_SN, LdapEncoder.filterEncode(query.naam()) + "*"));
		}
		if (isNotEmpty(query.volledigeNaam())) {
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_CN, LdapEncoder.filterEncode(query.volledigeNaam()) + "*"));
		}
		if (isNotEmpty(query.email())) {
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_MAIL, LdapEncoder.filterEncode(query.email()) + "*"));
		}
		if (isNotEmpty(query.oe())) {
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_VDAB_OE, LdapEncoder.filterEncode(query.oe())));
		}
		filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_EMPLOYEETYPE, "Derde"));

		LdapFilter ldapFilter = filterBuilder.build();
		log.debug("filter: {}", ldapFilter);

		List<IsimLdapPerson> persons;
		if (query.limit() != 0) {
			persons = isimLdapManager.getPersonsByFilter(ldapFilter, attrList, query.limit());
		} else {
			persons = isimLdapManager.getPersonsByFilter(ldapFilter, attrList);
		}
		return this.personConverter.convertList(persons, false);
	}


	private List<String> getAttrList() {
		return List.of(
				IsimAttributeNames.ATTR_CN,
				IsimAttributeNames.ATTR_EMPLOYEENUMBER,
				IsimAttributeNames.ATTR_GIVENNAME,
				IsimAttributeNames.ATTR_SN,
				IsimAttributeNames.ATTR_PARENT,
				IsimAttributeNames.ATTR_UID,
				IsimAttributeNames.ATTR_VDABUID,
				IsimAttributeNames.ATTR_AUTHENTICATIONLEVEL,
				IsimAttributeNames.ATTR_PERSONSTATUS,
				IsimAttributeNames.ATTR_IKP,
				IsimAttributeNames.ATTR_BEDRIJFSNAAM,
				IsimAttributeNames.ATTR_MAIL,
				IsimAttributeNames.ATTR_EMPLOYEETYPE,
				IsimAttributeNames.ATTR_VDAB_DELETE_DESCRIPTION,
				IsimAttributeNames.ATTR_ISAM_ACCOUNT_STATUS
		);
	}

	private List<RoleObject> getRolesWithPending(PersonObject personObject, AdminDomainObject adminDomainObject) {
		if (log.isDebugEnabled()) {
			String buf = "getRolesWithPending PersonObject: %s %s AdmindomainObject: %s".formatted(
					personObject.getCommonName(), personObject.getDn(), adminDomainObject.getName());
			log.debug(buf);
		}

		List<RoleObject> roles = adminDomainObject.getRoles();

		List<String> pendingRoles = this.taskService.getPendingRoles(personObject.getUserId());

		if (personObject.getRoles() == null) {
			return new ArrayList<>(0);
		}

		List<RoleObject> userRoles = new ArrayList<>(roles.size());
		Set<String> userRolesSet = new HashSet<>();

		if (personObject.getRoles() != null) {
			userRolesSet = personObject.getRoles().stream().map(RoleObject::getRoleName).collect(Collectors.toSet());
		} else {
			if (log.isDebugEnabled()) {
				log.debug("person no roles");
			}
		}

		for (RoleObject adminRoleObject : roles) {
			boolean sameRole = false;
			boolean hasPrerequisite = false;

			for (RoleObject userRole : personObject.getRoles()) {
				if (adminRoleObject.getRoleName().equals(userRole.getRoleName())) {
					sameRole = true;

					break;
				} else {
					RoleObject prerequisiteRole = this.personConverter.getPrerequisiteRole(adminRoleObject.getRoleName());
					if (prerequisiteRole != null && prerequisiteRole.getRoleName().equals(userRole.getRoleName()) && !userRolesSet.contains(adminRoleObject.getRoleName())) {
						// heeft de _prerequisite role maar nog niet de gewone
						hasPrerequisite = true;
						break;
					}
				}
			}

			RoleObject userRoleObject = new RoleObject();
			userRoleObject.setAdminRole(adminRoleObject.isAdminRole());
			userRoleObject.setDn(adminRoleObject.getDn());
			userRoleObject.setHasRole(false);
			userRoleObject.setAvailable(true);
			userRoleObject.setChanged(false);
			userRoleObject.setRoleDescription(adminRoleObject.getRoleDescription());
			userRoleObject.setRoleName(adminRoleObject.getRoleName());
			userRoleObject.setVdabRoleDescription(adminRoleObject.getVdabRoleDescription());
			userRoleObject.setVdabRoleName(adminRoleObject.getVdabRoleName());
			userRoleObject.setPending(false);

			if (sameRole || hasPrerequisite) {
				userRoleObject.setHasRole(true);
			}

			if (hasPrerequisite) {
				// heeft de _prerequisite role maar nog niet de gewone
				userRoleObject.setPending(true);
			}

			userRoles.add(userRoleObject);
		}

		if (pendingRoles != null && !pendingRoles.isEmpty()) {
			// pending roles via approval task
			userRoles.stream().filter(r -> pendingRoles.contains(r.getRoleName())).forEach(r -> {
				r.setHasRole(true);
				r.setPending(true);
			});
		}

		return userRoles;
	}

	@Override
	public boolean rrnExists(String rrn, Dn distinguishedName) {
		log.debug("rrnExists rijksregisternummer: {} distinguishedname: {}", LogSanitizer.sanitize(rrn), LogSanitizer.sanitize(distinguishedName.toString()));
		return isimLdapManager.getPersonDnByRrnAndAdminDomainDn(rrn, distinguishedName).isPresent();
	}

	@Override
	public List<PersonObject> findPersonsInPrullenbakForIkpNummer(Ikp ikpNummer) {
		log.debug("findPersonsInPrullenbakForIkpNummer ikpNummer: {}", LogSanitizer.sanitize(ikpNummer.toString()));

		LdapFilter ldapFilter = LdapFilter.andFilter(
				LdapFilter.objectClassFilter(IsimObjectClasses.VDAB_DERDE),
				LdapFilter.equalityFilter(IsimAttributeNames.ATTR_IKP, ikpNummer),
				LdapFilter.equalityFilter(IsimAttributeNames.ATTR_PARENT, isimLdapManager.getPrullenbakDn())
		);

		List<IsimLdapPerson> persons = this.isimLdapManager.getPersonsByFilter(ldapFilter, DEFAULT_PERSON_ATTRIBUTES_DERDEN);
		return personConverter.convertList(persons, false);
	}

	@Override
	public Dn getPersonDnInPrullenbakForRrnAndIkpNummer(String rrn, Ikp ikpNummer) {
		log.debug("getPersonDnInPrullenbakForRrnAndIkpNummer rijksregisternummer: {} ikpNummer: {}", LogSanitizer.sanitize(rrn), LogSanitizer.sanitize(ikpNummer.toString()));

		LdapFilter ldapFilter = LdapFilter.andFilter(
				LdapFilter.objectClassFilter(IsimObjectClasses.VDAB_DERDE),
				LdapFilter.equalityFilter(IsimAttributeNames.ATTR_IKP, ikpNummer),
				LdapFilter.equalityFilter(IsimAttributeNames.ATTR_RIJKSREGISTERNUMMER, rrn),
				LdapFilter.equalityFilter(IsimAttributeNames.ATTR_PARENT, isimLdapManager.getPrullenbakDn())
		);

		List<Dn> persons = this.isimLdapManager.getPersonsDnsByFilter(ldapFilter);
		return !persons.isEmpty() ? persons.getFirst() : null;
	}

	@Override
	public boolean changeLoginMethod(String personGlobalId, String loginMethod) {
		if (log.isDebugEnabled()) {
			log.debug("changeLoginMethod personId: {} loginmethod: {}", personGlobalId, LogSanitizer.sanitize(loginMethod));
		}

		IsimWsSession isimWsSession = isimUserContextManager.getSession();
		IsimWsPerson personOld = isimWsClient.getPersonByDn(isimWsSession, applicationProperties.createPersonDn(personGlobalId));
		IsimWsPerson personNew = personOld.setAuthenticationLevel(loginMethod);

		try {
			isimWsClient.updatePerson(isimWsSession, personOld, personNew)
					.ifPresent(request -> isimWsRequestService.waitUntilFinished(isimWsSession, request));
			return true;
		} catch (IsimRuntimeException e) {
			log.error("Error updating person", e);
			return false;
		}
	}

	@Override
	public void toevoegenRol(String gebruikersnaam, String rol) throws PersonNotFoundException, RoleNotFoundException {
		IsimLdapPerson person = isimLdapManager.getPersonByUid(gebruikersnaam, DEFAULT_PERSON_ATTRIBUTES_DERDEN).orElseThrow(PersonNotFoundException::new);

		PersonObject personObject = personConverter.convert(person, true);
		RoleObject role = this.roleService.findByRoleName(rol).orElseThrow(RoleNotFoundException::new);

		if (role == null) {
			throw new RoleNotFoundException(rol);
		}

		if (!heeftGebruikerRol(personObject, role)) {
			IsimWsSession isimWsSession = isimUserContextManager.getSession();

			isimRoleService.addRoles(isimWsSession, person.getDn(), Collections.singleton(role.getDn()));

			Dn adminDomainDn = person.getParentDn();
			adminDomainService.findAdminDomainByDnWithRoles(adminDomainDn)
					.ifPresent(adminDomainObject -> updatePersonCaches(person.getDn(), adminDomainObject));
		}
	}

	@Override
	public void verwijderenRol(String gebruikersnaam, String rol) throws PersonNotFoundException, RoleNotFoundException {
		IsimLdapPerson person = isimLdapManager.getPersonByUid(gebruikersnaam, DEFAULT_PERSON_ATTRIBUTES_DERDEN).orElseThrow(PersonNotFoundException::new);
		PersonObject personObject = personConverter.convert(person, true);

		RoleObject role = this.roleService.findByRoleName(rol).orElseThrow(RoleNotFoundException::new);

		if (role == null) {
			throw new RoleNotFoundException(rol);
		}

		if (heeftGebruikerRol(personObject, role)) {
			IsimWsSession isimWsSession = isimUserContextManager.getSession();

			isimRoleService.removeRoles(isimWsSession, person.getDn(), Collections.singleton(role.getDn()));

			Dn adminDomainDn = person.getParentDn();
			adminDomainService.findAdminDomainByDnWithRoles(adminDomainDn).ifPresent(adminDomainObject -> updatePersonCaches(person.getDn(), adminDomainObject));
		}
	}

	private boolean heeftGebruikerRol(PersonObject personObject, RoleObject role) {
		return personObject.getRoles().stream().filter(r -> role.getRoleName().equalsIgnoreCase(r.getRoleName())).count() == 1;
	}

	private void insertPersonInCaches(Dn personDn, AdminDomainObject adminDomain) {
		this.findPersonByDn(personDn, adminDomain).ifPresent(cacheService::addPersonToPersonCaches);
	}

	@Override
	public void updatePersonCaches(Dn personDn, AdminDomainObject adminDomain) {
		long start = System.currentTimeMillis();

		this.findPersonByDn(personDn, adminDomain).ifPresent(personObject -> this.cacheService.updatePersonInCache(personDn, personObject));

		if (log.isDebugEnabled()) {
			log.debug("duur updatePersonCaches({}) in {} sec", personDn, ((double) System.currentTimeMillis() - start) / 1000);
		}
	}

	@Override
	public List<PersonObject> getDerdeProfilesBy(String rrn) {

		LdapFilter ldapFilter = LdapFilter.andFilter(
				LdapFilter.objectClassFilter(IsimObjectClasses.VDAB_DERDE),
				LdapFilter.equalityFilter(IsimAttributeNames.ATTR_RIJKSREGISTERNUMMER, rrn)
		);
		List<String> attrList = getAttrList();

		List<IsimLdapPerson> ldapPersons = this.isimLdapManager.getPersonsByFilter(ldapFilter, attrList);
		return ldapPersons.stream()
				.map(pers -> personConverter.convert(pers, false))
				.collect(Collectors.toList());
	}

	private void removeWorkFlowProcessesForUser(IsimWsSession wsSession, Dn userDn, String justification) {
		taskService.getWorkFlowProcessesForUser(userDn).stream().map(WorkflowProcess::getProcessId)
				.forEach(p -> isimWsRequestService.abortRequest(wsSession, p, justification));
	}
}