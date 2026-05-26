package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.CacheNames;
import be.vdab.gebruikersbeheer.derden.converter.RoleConverter;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.RoleNotFoundException;
import be.vdab.gebruikersbeheer.derden.extension.FlashMap;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.util.LogSanitizer;
import be.vdab.gebruikersbeheer.derden.util.ValidateUtils;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.exception.IsimRuntimeException;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsPerson;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsRequest;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsRequestService;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.domain.IsimPerson;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

	private static final int DELAY_FOR_ROLES = 15;
	private static final int DEFAULT_BUFFER_LENGTH = 250;

	private final IsimLdapManager isimLdapManager;
	private final IsimWsClient isimWsClient;
	private final IsimWsRequestService isimWsRequestService;
	private final RoleConverter roleConverter;
	private final AdminService adminService;
	private final ValidateUtils validateUtils;
	private final CacheManager cacheManager;
	private final IsimUserContextManager isimContextManager;


	@Override
	@Cacheable(cacheNames = CacheNames.CACHE_ROLES)
	public List<RoleObject> findRoles() {
		List<RoleObject> roles = this.isimLdapManager.getNonDynamicRoles().stream()
				.map(roleConverter::convert)
				.collect(Collectors.toList());
		return roleConverter.convertRolesList(roles, false);
	}

	@Override
	public Optional<RoleObject> findRoleByDN(Dn roleDn) {

		String sanitizedRoleDn = LogSanitizer.sanitize(roleDn.getValue());
		log.trace("findRoleByDN roleDn: {}", sanitizedRoleDn);

		long start = System.currentTimeMillis();

		Optional<RoleObject> roleObject = this.isimLdapManager.getNonDynamicRoleByDn(roleDn).map(roleConverter::convert);

		long stap = System.currentTimeMillis();
		log.trace("duur {} sec voor {}", (((double) stap) - start) / 1000, LogSanitizer.sanitize(roleDn.toString()));


		return roleObject;
	}

	@Override
	public boolean isRoleInCache(Dn roleDn) {
		RoleObject roleFromCache = getRolesCache().get(roleDn, RoleObject.class);
		return roleFromCache != null;
	}

	@Override
	public void addRoleToCache(RoleObject roleObject) {
		if (log.isDebugEnabled()) {
			log.debug("add role {} to cache", roleObject.getRoleName());
		}

		getRolesCache().putIfAbsent(roleObject.getDn(), roleObject);
	}

	private Optional<RoleObject> findRoleByDnFromCache(Dn roleDn) {
		log.trace("findRoleByDnFromCache roleDn: {}", LogSanitizer.sanitize(roleDn.toString()));

		Cache cacheRoles = getRolesCache();

		RoleObject roleFromCache = cacheRoles.get(roleDn, RoleObject.class);
		if (roleFromCache != null) {
			return Optional.of(roleFromCache);
		}

		Optional<RoleObject> roleObject = findRoleByDN(roleDn);
		roleObject.ifPresent(role -> cacheRoles.put(roleDn, role));
		return roleObject;
	}

	@Override
	public void changePersonRoleChangeList(List<PersonObject> personObjects, RoleObject roleObject, AdminDomainObject adminDomainObject) {
		if (log.isDebugEnabled()) {
			StringBuilder buf = new StringBuilder(250);
			buf.append("changePersonRoleChangeList personObjects: ");

			if (personObjects != null) {
				int index = 0;

				for (PersonObject person : personObjects) {
					if (index > 0) {
						buf.append(" ");
					}

					if (person != null) {
						if (person.getDn() != null && person.getDn().getGlobalId() != null) {
							buf.append(person.getDn().getGlobalId());
						} else {
							buf.append("no dn");
						}
						buf.append(";");
						buf.append(person.getUserId());

						index++;
					}
				}
			} else {
				buf.append("/");
			}

			buf.append(" roleObject: ");
			if (roleObject != null) {
				buf.append(roleObject.getRoleName());
			} else {
				buf.append("/");
			}
			buf.append(" admindomainObject: ");
			if (adminDomainObject != null) {
				buf.append(adminDomainObject.getDn().getGlobalId());
				buf.append(" ");
				buf.append(adminDomainObject.getName());
			} else {
				buf.append("/");
			}

			log.debug(LogSanitizer.sanitize(buf.toString()));
		}

		if (roleObject == null) {
			log.error("changePersonRoleChangeList RoleObject NULL");
		}

		executeRoleChangeForMultiplePersons(Objects.requireNonNull(personObjects), Objects.requireNonNull(roleObject), adminDomainObject);
	}

	@Override
	public void addAndRemoveRoles(PersonObject personObject, AdminDomainObject adminDomainObject) {
		if (log.isTraceEnabled()) {
			StringBuilder msg = new StringBuilder(250);
			msg.append("addAndRemoveRoles person: ");
			msg.append(personObject.getCommonName());
			msg.append(" roles: ");

			int index = 0;
			for (RoleObject roleObject : personObject.getRoles()) {
				if (index > 0) {
					msg.append(',');
				}

				msg.append(roleObject.getRoleName());

				index++;
			}
			msg.append(" admindomainObject: ");
			msg.append(adminDomainObject.toString());

			log.trace(LogSanitizer.sanitize(msg.toString()));
		}

		executeRoleChangeForSinglePerson(personObject, personObject.getRoles(), adminDomainObject);
	}

	private void executeRoleChangeForMultiplePersons(@NonNull List<PersonObject> personsToChange, @NonNull RoleObject roleObject, AdminDomainObject organization) {
		if (log.isDebugEnabled()) {
			StringBuilder msg = new StringBuilder(250);
			msg.append("executeRoleChange personObjects: ");

			int index = 0;
			for (PersonObject personObject : personsToChange) {
				if (personObject != null) {
					if (index > 0) {
						msg.append(',');
					}

					msg.append(personObject.getCommonName());

					index++;
				} else {
					log.error("Person null");
				}
			}

			msg.append(" role: ").append(roleObject.getRoleName());

			msg.append(" admindomainObject: ");

			if (organization != null) {
				msg.append(organization.getDn().getGlobalId());
				msg.append(" ");
				msg.append(organization.getName());
			} else {
				msg.append("/");
			}

			log.debug(LogSanitizer.sanitize(msg.toString()));
		}

		if (organization == null) {
			log.error("adminDomainObject NULL");
		}

		int adminsToegevoegd = 0;
		int nbrOfAdminsUnderOrganization = getAantalAdmins(organization);

		if (roleObject.isAdminRole()) {
			// eerst nakijken hoeveel admins er overblijven na de wijzigingen.
			int overblijvendAantalAdmins = 0;

			for (PersonObject personToChange : personsToChange) {
				if (personToChange != null && !personToChange.isVirtualAccount() && personToChange.getHasRole()) {
					overblijvendAantalAdmins++;
				}
			}

			log.debug("aantal overblijvende admins: {}", overblijvendAantalAdmins);

			//we zijn bezig met het toekennen van 1 rol aan meerdere gebruikers
			//hier controleren we vooraf aangezien de GUI de andere gevallen via javascript al tegenhoudt.
			if (overblijvendAantalAdmins > this.validateUtils.maxAantalAdmins()) {
				FlashMap.setErrorMessage("errorMessageMaxAdmins", "De 'Domain Administrators' rol werd niet toegekend aangezien het maximaal aantal admins reeds bereikt was.");
				return; //gewoon afbreken
			} else if (overblijvendAantalAdmins < this.validateUtils.minAantalAdmins()) {
				FlashMap.setErrorMessage("errorMessageMinAdmins", "De 'Domain Administrators' rol werd niet verwijderd aangezien er minimum 1 admin per vestiging moet zijn.");
				return; //gewoon afbreken
			}
		}

		IsimWsSession isimWsSession = isimContextManager.getSession();

		for (PersonObject personObject : personsToChange) {
			if (personObject == null) {
				log.error("Person null");
				continue;
			}
			if (personObject.getDn() != null) {
				IsimLdapPerson personOld = isimLdapManager.getPersonByDn(personObject.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN)
						.orElseThrow(PersonNotFoundException::new);

				ArrayList<RoleObject> addRoles = new ArrayList<>();
				ArrayList<RoleObject> deleteRoles = new ArrayList<>();

				RoleObject cvsRoleObject = null;
				if (roleObject.isChanged()) {
					if (roleObject.isAdminRole()) {
						// administrator role
						if (roleObject.getHasRole()) {
							if (personHasRole(personOld, roleObject)) {
								log.debug("Person {} had reeds rol {} -> niets doen", LogSanitizer.sanitize(personObject.getUserId()), LogSanitizer.sanitize(roleObject.getVdabRoleName()));
							} else {
								if (this.validateUtils.maxAdminsReached(nbrOfAdminsUnderOrganization + adminsToegevoegd)) {
									FlashMap.setWarningMessage("errorMessageMaxAdmins", "De 'Domain Administrators' rol werd niet toegend aangezien het maximaal aantal admins reeds bereikt was.");
								} else {
									adminService.createNewAdmin(organization, personObject);
									adminsToegevoegd++;
									addRoles.add(roleObject);
								}
							}
						} else {
							if (this.validateUtils.minAdminsReached(nbrOfAdminsUnderOrganization + adminsToegevoegd)) {
								FlashMap.setWarningMessage("errorMessageMinAdmins", "De 'Domain Administrators' rol werd niet verwijderd aangezien er minimum 1 admin per vestiging moet zijn.");
							} else {
								adminService.deleteAdmin(organization, personObject);
								adminsToegevoegd--;
								deleteRoles.add(roleObject);
							}
						}
					} else {
						if (roleObject.getHasRole()) {
							if (personHasRole(personOld, roleObject)) {
								log.debug("Person {} had reeds rol {} -> niets doen", LogSanitizer.sanitize(personObject.getUserId()), LogSanitizer.sanitize(roleObject.getVdabRoleName()));
							} else {
								// CVS rol ?
								if (this.validateUtils.isCVSRole(roleObject)) {
									if (!roleObject.isPending()) {
										// CVS rol apart submitten indien niet pending (anders dubbel)
										cvsRoleObject = roleObject;
									}
								} else {
									addRoles.add(roleObject);
								}
							}
						} else {
							deleteRoles.add(roleObject);
						}
					}
				} else if (personObject.isChanged()) {
					// = toewijzen van een rol aan meerdere personen

					// LET OP : DE CONTROLE OP MAX AANTAL gebeurd hier in combinatie met javascript langs
					// client side. Indien het uiteindelijk overblijvend aantal in orde is, worden de acties uitgevoerd !
					if (roleObject.isAdminRole()) {
						if (personObject.getHasRole()) {
							adminService.createNewAdmin(organization, personObject);
							adminsToegevoegd++;
							addRoles.add(roleObject);
						} else {
							adminService.deleteAdmin(organization, personObject);
							adminsToegevoegd--;
							deleteRoles.add(roleObject);
						}
					} else {
						if (personObject.getHasRole()) {
							// CVS rol ?
							if (this.validateUtils.isCVSRole(roleObject)) {
								// CVS rol apart submitten
								cvsRoleObject = roleObject;
							} else {
								addRoles.add(roleObject);
							}
						} else {
							deleteRoles.add(roleObject);
						}
					}
				}

				boolean cvsRoleToevoegen = false;

				if (cvsRoleObject != null) {
					Dn dnCvsRol = cvsRoleObject.getDn();

					Set<Dn> rolesPerson = personOld.getRollen();
					List<Dn> listCVSRole = rolesPerson.stream().filter(dnCvsRol::equals).collect(Collectors.toList());

					if (listCVSRole.size() != 1) {
						cvsRoleToevoegen = true;
					} else {
						log.trace("person {} has already CVS Role", personOld.getDn());
					}
				}

				changePersonRoles(isimWsSession, personOld, personObject, addRoles, deleteRoles);

				if (cvsRoleToevoegen) {
					changePersonRole(isimWsSession, personObject, cvsRoleObject, true);
				}
			} else {
				log.error("Person dn null for {}", LogSanitizer.sanitize(personObject.getCommonName()));
			}
		}
	}

	private void executeRoleChangeForSinglePerson(@NonNull PersonObject personToChange, @NonNull List<RoleObject> rolesToChange, AdminDomainObject organization) {
		if (log.isDebugEnabled()) {
			StringBuilder msg = new StringBuilder(250);
			msg.append("executeRoleChange personObject: ").append(personToChange.getCommonName());

			msg.append(" roles: ");
			int index = 0;
			for (RoleObject roleObject : rolesToChange) {
				if (index > 0) {
					msg.append(',');
				}

				msg.append(roleObject.getRoleName());

				index++;
			}

			msg.append(" admindomainObject: ");

			if (organization != null) {
				msg.append(organization.getDn().getGlobalId());
				msg.append(" ");
				msg.append(organization.getName());
			} else {
				msg.append("/");
			}

			log.debug(LogSanitizer.sanitize(msg.toString()));
		}

		if (organization == null) {
			log.error("adminDomainObject NULL");
		}

		int adminsToegevoegd = 0;
		int nbrOfAdminsUnderOrganization = getAantalAdmins(organization);

		if (personToChange.getDn() == null) {
			log.error("Person dn null for {}", LogSanitizer.sanitize(personToChange.getCommonName()));
			return;
		}

		IsimLdapPerson personOld = isimLdapManager.getPersonByDn(personToChange.getDn(), DEFAULT_PERSON_ATTRIBUTES_DERDEN)
				.orElseThrow(PersonNotFoundException::new);

		ArrayList<RoleObject> addRoles = new ArrayList<>();
		ArrayList<RoleObject> deleteRoles = new ArrayList<>();

		RoleObject cvsRoleObject = null;
		for (RoleObject roleObject : rolesToChange) {
			if (roleObject.isChanged()) {
				if (roleObject.isAdminRole()) {
					if (roleObject.getHasRole()) {
						if (personHasRole(personOld, roleObject)) {
							log.debug("Person {} had reeds rol {} -> niets doen", LogSanitizer.sanitize(personToChange.getUserId()), LogSanitizer.sanitize(roleObject.getVdabRoleName()));
						} else {
							if (this.validateUtils.maxAdminsReached(nbrOfAdminsUnderOrganization)) {
								FlashMap.setWarningMessage("errorMessageMaxAdmins", "De 'Domain Administrators' rol werd niet toegend aangezien het maximaal aantal admins reeds bereikt was.");
							} else {
								adminService.createNewAdmin(organization, personToChange);
								adminsToegevoegd++;
								addRoles.add(roleObject);
							}
						}
					} else {
						if (this.validateUtils.minAdminsReached(nbrOfAdminsUnderOrganization)) {
							FlashMap.setWarningMessage("errorMessageMinAdmins", "De 'Domain Administrators' rol werd niet verwijderd aangezien er minimum 1 admin per vestiging moet zijn.");
						} else {
							adminService.deleteAdmin(organization, personToChange);
							adminsToegevoegd--;
							deleteRoles.add(roleObject);
						}
					}
				} else { // role is not admin role
					if (roleObject.getHasRole()) {
						if (personHasRole(personOld, roleObject)) {
							log.debug("Person {} had reeds rol {} -> niets doen", LogSanitizer.sanitize(personToChange.getUserId()), LogSanitizer.sanitize(roleObject.getVdabRoleName()));
						} else {
							// CVS rol ?
							if (this.validateUtils.isCVSRole(roleObject)) {
								if (!roleObject.isPending()) {
									// CVS rol apart submitten indien niet pending (anders dubbel)
									cvsRoleObject = roleObject;
								}
							} else {
								addRoles.add(roleObject);
							}
						}
					} else {
						deleteRoles.add(roleObject);
					}
				}
			} else if (personToChange.isChanged()) {
				if (roleObject.isAdminRole()) {
					// administrator role
					if (personToChange.getHasRole()) {
						if (this.validateUtils.maxAdminsReached(nbrOfAdminsUnderOrganization + adminsToegevoegd)) {
							FlashMap.setWarningMessage("errorMessageMaxAdmins", "De 'Domain Administrators' rol werd niet toegekend aangezien het maximaal aantal admins reeds bereikt was.");
						} else {
							adminService.createNewAdmin(organization, personToChange);
							adminsToegevoegd++;
							addRoles.add(roleObject);
						}
					} else {
						if (this.validateUtils.minAdminsReached(nbrOfAdminsUnderOrganization + adminsToegevoegd)) {
							FlashMap.setWarningMessage("errorMessageMinAdmins", "De 'Domain Administrators' rol werd niet verwijderd aangezien er minimum 1 admin per vestiging moet zijn.");
						} else {
							adminService.deleteAdmin(organization, personToChange);
							adminsToegevoegd--;
							deleteRoles.add(roleObject);
						}
					}
				} else {
					if (personToChange.getHasRole()) {
						// CVS rol ?
						if (this.validateUtils.isCVSRole(roleObject)) {
							// CVS rol apart submitten
							cvsRoleObject = roleObject;
						} else {
							addRoles.add(roleObject);
						}
					} else {
						deleteRoles.add(roleObject);
					}
				}
			}
		}

		boolean cvsRoleToevoegen = false;

		if (cvsRoleObject != null) {
			Dn dnCvsRol = cvsRoleObject.getDn();

			Set<Dn> rolesPerson = personOld.getRollen();
			List<Dn> listCVSRole = rolesPerson.stream().filter(dnCvsRol::equals).collect(Collectors.toList());

			if (listCVSRole.size() != 1) {
				cvsRoleToevoegen = true;
			} else {
				log.trace("person {} has already CVS Role", personOld.getDn());
			}
		}

		IsimWsSession isimWsSession = isimContextManager.getSession();
		changePersonRoles(isimWsSession, personOld, personToChange, addRoles, deleteRoles);

		if (cvsRoleToevoegen) {
			changePersonRole(isimWsSession, personToChange, cvsRoleObject, true);
		}
	}

	@Override
	public int getAantalAdmins(AdminDomainObject adminDomainObject) {
		int size = 0;
		if (adminDomainObject != null) {
			for (PersonObject personObj : adminDomainObject.getAdministrators()) {
				// VDAB virtual accounts niet meerekenen, deze zijn altijd admin
				// en verdwijnen binnenkort
				if (!"vdabvirtual".equalsIgnoreCase(personObj.getProfileName())) { // We should use personObj.isVirtualAccount() here?
					size++;
				}
			}
		}

		if (log.isDebugEnabled() && adminDomainObject != null) {
			log.debug("Admins {} : {}", adminDomainObject.getName(), size);
		}

		return size;
	}

	@Override
	public void changePersonRoles(IsimWsSession isimWsSession, IsimPerson oldPerson, PersonObject newPerson, List<RoleObject> rolesToAdd, List<RoleObject> rolesToDelete) {
		String currentVdabCvsRol = oldPerson.getCvsrole() != null ? oldPerson.getCvsrole() : "";
		String newVdabCvsRol = newPerson.getVdabCvsRol() != null ? newPerson.getVdabCvsRol() : "";

		boolean vdabCvsRolChanged = (currentVdabCvsRol != null && !currentVdabCvsRol.equals(newVdabCvsRol));
		boolean personModified = false;

		if (vdabCvsRolChanged) {
			IsimWsPerson personOld = isimWsClient.getPersonByDn(isimWsSession, oldPerson.getDn());
			IsimWsPerson personNew = personOld.setCvsRol(newPerson.getVdabCvsRol());
			IsimWsRequest isimWsRequest = isimWsClient.updatePerson(isimWsSession, personOld, personNew).orElseThrow(this::createExpectedRequestException);

			isimWsRequestService.waitUntilProcessingHasStarted(isimWsSession, isimWsRequest, DELAY_FOR_ROLES);
			waitForPersonModification(isimWsSession, isimWsRequest);

			personModified = true;
		}

		StringBuilder msg = new StringBuilder(DEFAULT_BUFFER_LENGTH).append("changePersonRoles '").append(oldPerson.getCn()).append("' add: ");

		Set<Dn> oldPersonRollen = oldPerson.getRollen();

		int msgIndex = 0;
		if (rolesToAdd != null && !rolesToAdd.isEmpty()) {
			List<Dn> rolesToAddWithoutApproval = new ArrayList<>();
			List<Dn> rolesToAddWithApproval = new ArrayList<>();

			for (RoleObject roleToAdd : rolesToAdd) {
				if (!oldPersonRollen.contains(roleToAdd.getDn())) {
					if (msgIndex > 0) {
						msg.append(',');
					}
					msg.append(roleToAdd.getRoleName());
					msgIndex++;

					RoleObject temp = findRoleByDnFromCache(roleToAdd.getDn()).orElseThrow(RoleNotFoundException::new);
					if (temp.isNeedsApproval()) {
						rolesToAddWithApproval.add(roleToAdd.getDn());
					} else {
						rolesToAddWithoutApproval.add(roleToAdd.getDn());
					}
				}
			}

			if (!rolesToAddWithoutApproval.isEmpty()) {
				IsimWsRequest isimWsRequest = isimWsClient.addRolesToPerson(isimWsSession, newPerson.getDn(), rolesToAddWithoutApproval).orElseThrow(this::createExpectedRequestException);
				isimWsRequestService.waitUntilProcessingHasStarted(isimWsSession, isimWsRequest, DELAY_FOR_ROLES);
				waitForPersonModification(isimWsSession, isimWsRequest);

				personModified = true;
			}
			if (!rolesToAddWithApproval.isEmpty()) {
				addRolesWithApprovalToPerson(isimWsSession, oldPerson, rolesToAddWithApproval);

				personModified = true;
			}
		} else {
			msg.append("/");
		}

		msg.append(" delete: ");

		msgIndex = 0;
		if (rolesToDelete != null && !rolesToDelete.isEmpty()) {
			List<Dn> deleteRoles = new ArrayList<>();
			for (RoleObject roleObject : rolesToDelete) {
				if (msgIndex > 0) {
					msg.append(',');
				}
				msg.append(roleObject.getRoleName());
				msgIndex++;

				if (oldPersonRollen.contains(roleObject.getDn())) {
					deleteRoles.add(roleObject.getDn());
				}
			}

			if (!deleteRoles.isEmpty()) {
				IsimWsRequest isimWsRequest = isimWsClient.removeRolesFromPerson(isimWsSession, newPerson.getDn(), deleteRoles).orElseThrow(this::createExpectedRequestException);
				isimWsRequestService.waitUntilProcessingHasStarted(isimWsSession, isimWsRequest, DELAY_FOR_ROLES);
				waitForPersonModification(isimWsSession, isimWsRequest);

				personModified = true;
			}
		} else {
			msg.append("/");
		}

		log.debug("{}", LogSanitizer.sanitize(msg.toString()));

		if (personModified) {
			Cache cache = this.cacheManager.getCache(CacheNames.CACHE_PERSONS);
			if (cache != null) {
				log.trace("Remove person {} {} from cache", LogSanitizer.sanitize(newPerson.getUserId()), LogSanitizer.sanitize(newPerson.getDn().toString()));
				cache.evict(newPerson.getDn());
			}
		}
	}

	@Override
	public void changePersonRole(PersonObject person, RoleObject roleObject, boolean attach) {
		IsimWsSession isimWsSession = isimContextManager.getSession();
		changePersonRole(isimWsSession, person, roleObject, attach);
	}

	@Override
	public void changePersonRole(IsimWsSession isimWsSession, PersonObject person, RoleObject roleObject, boolean attach) {
		if (log.isTraceEnabled()) {
			log.trace("changePersonRole person: {} roleObject: {}", person.getDn().getGlobalId(), LogSanitizer.sanitize(roleObject.getRoleName()));
		}

		if (roleObject != null) {
			IsimWsRequest isimWsRequest = modifyRoleMember(isimWsSession, person.getDn(), roleObject.getDn(), attach).orElseThrow(this::createExpectedRequestException);
			isimWsRequestService.waitUntilProcessingHasStarted(isimWsSession, isimWsRequest);

			if ("CVS".equals(roleObject.getRoleName())) {
				checkActivityAddCVS_prerequisiteRole(isimWsSession, isimWsRequest);
			}

			Cache cache = this.cacheManager.getCache(CacheNames.CACHE_PERSONS);
			if (cache != null) {
				cache.evict(person.getDn());
			}
		}
	}

	public Optional<IsimWsRequest> modifyRoleMember(IsimWsSession session, Dn personDn, Dn roleDn, boolean add) {
		if (add) {
			return isimWsClient.addRoleToPerson(session, personDn, roleDn);
		} else {
			return isimWsClient.removeRoleFromPerson(session, personDn, roleDn);
		}
	}

	@Override
	public Optional<RoleObject> findByRoleName(String roleName) {
		return Optional.ofNullable(roleName)
				.filter(StringUtils::isNotEmpty)
				.flatMap(isimLdapManager::getNonDynamicRoleByName)
				.map(roleConverter::convert);
	}

	@Override
	public Optional<RoleObject> findRoleByGlobalId(String globalId) {
		if (log.isDebugEnabled()) {
			log.debug("findRoleByGlobalId globalid: {}", globalId);
		}

		return isimLdapManager.getNonDynamicRoleByGlobalId(globalId)
				.map(roleConverter::convert);
	}

	@Override
	public List<RoleObject> findRolesByGlobalId(List<String> globalIds) {
		return isimLdapManager.getNonDynamicRolesByGlobalIds(globalIds).stream()
				.map(roleConverter::convert)
				.collect(Collectors.toList());
	}

	@Override
	public RoleObject findAdminRole() {
		log.debug("findAdminRole");

		RoleObject roleObject = findByRoleName(RoleNames.ROL_DOMAIN_ADMINS).orElseThrow(RoleNotFoundException::new);
		roleObject.setHasRole(false);

		return roleObject;
	}

	private void waitForPersonModification(IsimWsSession isimWsSession, IsimWsRequest isimWsRequest) {
		isimWsRequestService.waitForActivityToComplete(isimWsSession, isimWsRequest, "MODIFYPERSON");
	}

	private void checkActivityAddCVS_prerequisiteRole(IsimWsSession isimWsSession, IsimWsRequest isimWsRequest) {
		isimWsRequestService.waitForActivityToComplete(isimWsSession, isimWsRequest, "AddCVS_prerequisiteRole");
	}

	@Override
	public boolean personHasRole(IsimPerson person, RoleObject roleObject) {
		Set<Dn> rolesPerson = person.getRollen();

		Set<Dn> personGefilterdeRoles = rolesPerson.stream().filter(r -> r.equals(roleObject.getDn())).collect(Collectors.toSet());
		return personGefilterdeRoles.size() == 1;
	}

	private void addRolesWithApprovalToPerson(IsimWsSession isimWsSession, IsimPerson person, List<Dn> addRolesWithApproval) {
		if (addRolesWithApproval != null) {
			addRolesWithApproval.forEach(roleWithApproval -> {
				IsimWsRequest request = isimWsClient.addRoleToPerson(isimWsSession, person.getDn(), roleWithApproval).orElseThrow(this::createExpectedRequestException);
				isimWsRequestService.waitUntilProcessingHasStarted(isimWsSession, request, DELAY_FOR_ROLES);
			});
		}
	}

	private IsimRuntimeException createExpectedRequestException() {
		return new IsimRuntimeException("Expected a request to be created in ISIM");
	}

	private Cache getRolesCache() {
		Cache cache = cacheManager.getCache(CacheNames.CACHE_ROLES);
		if (cache == null) {
			throw new IllegalArgumentException("Roles cache not found");
		}
		return cache;
	}
}
