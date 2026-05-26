package be.vdab.gebruikersbeheer.derden.converter;

import be.vdab.gebruikersbeheer.derden.components.oeservice.OEServiceClient;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.service.PersonOrganizationService;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.derden.util.ValidateUtils;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.domain.IsimAdminDomain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AdminDomainConverter extends BaseConverter implements Converter<IsimAdminDomain, AdminDomainObject> {

	private final ValidateUtils validateUtils;
	private final OEServiceClient oeServiceClient;
	private final ApplicationProperties applicationProperties;
	private final PersonOrganizationService personOrganizationService;

	public AdminDomainConverter(RoleService roleService,
	                            ValidateUtils validateUtils,
	                            OEServiceClient oeServiceClient,
	                            ApplicationProperties applicationProperties,
	                            PersonOrganizationService personOrganizationService) {
		super(roleService);
		this.validateUtils = validateUtils;
		this.oeServiceClient = oeServiceClient;
		this.applicationProperties = applicationProperties;
		this.personOrganizationService = personOrganizationService;
	}

	public List<AdminDomainObject> convertList(List<? extends IsimAdminDomain> adminDomains) {
		return convertList(adminDomains, false, false);
	}

	public List<AdminDomainObject> convertList(List<? extends IsimAdminDomain> adminDomains, boolean convertRoles, boolean fetchOnlyVestigingData) {
		if (adminDomains == null) return Collections.emptyList();

		return adminDomains.stream()
				.map(adminDomain -> convert(adminDomain, convertRoles, fetchOnlyVestigingData))
				.collect(Collectors.toList());
	}

	public AdminDomainObject convert(@NonNull IsimAdminDomain adminDomain) {
		return convert(adminDomain, false, false);
	}

	public AdminDomainObject convert(IsimAdminDomain adminDomain, boolean convert) {
		return convert(adminDomain, convert, false);
	}
	public AdminDomainObject convert(IsimAdminDomain adminDomain, boolean convert, boolean fetchOnlyVestigingData) {
		if (adminDomain == null) return null;

		AdminDomainObject adminDomainObject = new AdminDomainObject(adminDomain.getDn());
		adminDomainObject.setGlobalId(adminDomain.getGlobalId().toString());
		adminDomainObject.setOu(adminDomain.getName());
		adminDomainObject.setName(adminDomain.getName());
		adminDomainObject.setPostalcode(adminDomain.getPostcode());
		adminDomainObject.setCity(adminDomain.getGemeente());
		if (adminDomain.getOe() != null) {
			adminDomainObject.setVdaboe(adminDomain.getOe().toString());

			String oeOmschrijving= this.oeServiceClient.getPubliekeOmschrijving(adminDomain.getOe());
			if (StringUtils.isEmpty(oeOmschrijving)){
				oeOmschrijving= "onbekende OE";
			}

			adminDomainObject.setOeName("OE: %s %s".formatted(adminDomain.getOe(), oeOmschrijving));
		}
		if (adminDomain.getIkp() != null) {
			adminDomainObject.setHeadQuarter(adminDomain.getIkp().isHoofdvestiging());
			adminDomainObject.setIkp(adminDomain.getIkp());
		}
		adminDomainObject.setStreet(adminDomain.getAdres());
		adminDomainObject.setKboNummer(adminDomain.getKbo());
		adminDomainObject.setParent(adminDomain.getParentDn());
		adminDomainObject.setSamakks(adminDomain.getSamenwerkingsakkoorden());

		if (!fetchOnlyVestigingData) {
			Collection<Dn> administrators = adminDomain.getAdministrators();
			Set<Dn> roles = adminDomain.getRollen();

			convert(adminDomainObject, administrators, roles, convert);
		}
		return adminDomainObject;
	}

	private void convert(AdminDomainObject adminDomainObject, Collection<Dn> administrators, Set<Dn> roles, boolean convert) {
		if (administrators != null) {
			if ("ldv".equalsIgnoreCase(this.applicationProperties.getZuil())) {
				List<Dn> adminsChecked = new ArrayList<>();
				for (Dn admin : administrators) {
					if (personOrganizationService.existsPersonInOrganization(admin, adminDomainObject.getDn())) {
						adminsChecked.add(admin);
					} else {
						log.error("Administrator {} does not exists anymore for admindomain {}", admin, adminDomainObject.getName());
					}
				}

				administrators = adminsChecked;
			}

			adminDomainObject.setAdministrators(getPersons(administrators));
		}

		if (convert) {
			RoleObject adminRole = roleService.findAdminRole();

			if (roles != null && !roles.isEmpty()) {
				List<RoleObject> roleObjects = getRoles(roles);
				int size = roleService.getAantalAdmins(adminDomainObject);
				boolean maxAdminsReached = this.validateUtils.maxAdminsReached(size);
				for (RoleObject roleObject : roleObjects) {
					if (!roleObject.isAdminRole()) {
						roleObject.setAvailable(true);
					} else {
						if (maxAdminsReached) {
							roleObject.setAvailable(false);
						}
					}
				}

				if (adminRole != null) {
					final String adminRoleName = adminRole.getRoleName();
					List<RoleObject> domainAdminRoles = roleObjects.stream().filter(r -> r.getRoleName().equals(adminRoleName)).collect(Collectors.toList());

					if (domainAdminRoles.size() == 1) {
						adminRole = domainAdminRoles.getFirst();
					} else {
						roleObjects.add(adminRole);
					}

					adminRole.setAdminRole(true);
					adminRole.setAvailable(!maxAdminsReached);
				}

				Collections.sort(roleObjects);
				adminDomainObject.setRoles(roleObjects);
			} else {
				List<RoleObject> roleObjects = new ArrayList<>();

				if (adminRole != null) {
					adminRole.setAdminRole(true);
					adminRole.setAvailable(true);

					roleObjects.add(adminRole);
				}

				adminDomainObject.setRoles(roleObjects);
			}
		}
	}

	@NonNull
	private List<PersonObject> getPersons(Collection<Dn> personDns) {
		log.debug("getPersons {}", personDns);

		List<Dn> dns = new ArrayList<>();

		if (personDns != null) {
			dns = personDns.stream().filter(Objects::nonNull).collect(Collectors.toList());
		}

		long start = System.currentTimeMillis();
		List<PersonObject> personsList = personOrganizationService.findPersonsByDn(dns);

		if (log.isTraceEnabled()) {
			log.trace("duur personen: {} sec.", ((double) System.currentTimeMillis() - start) / 1000);
		}

		return personsList;
	}
}
