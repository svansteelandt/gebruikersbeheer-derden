package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.converter.AdminDomainConverter;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainSearch;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.VestigingenZoekResultaat;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Hoofdzetelnummer;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.common.domain.Volgnummer;
import be.vdab.gebruikersbeheer.util.exception.OrganizationNotFoundException;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapAdminDomain;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.LdapFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames.DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_EXCL_ROLES;
import static be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames.DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_INCL_ROLES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AdminDomainServiceTest {

	private static final String ADMIN_NAME = "Testbedrijf";
	private static final String ADMIN_STREET = "Keizerslaan 31";
	private static final String ADMIN_POSTALCODE = "1000";
	private static final String ADMIN_CITY = "Brussel";
	private static final String ADMIN_IKP = "203000";
	private static final String ADMIN_IKP_END = "0";
	private static final String ADMIN_KBONUMMER = "0123.456.789";
	private static final String ADMIN_OE = "10008045";
	private static final Ikp IKP = Ikp.of(Hoofdzetelnummer.of("203"), Volgnummer.of("0"));
	private static final Dn ORGANIZATION_DN = new Dn("erglobalid=123,ou=orgchart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");
	private static final AdminDomainObject ADMIN_DOMAIN_OBJECT = new AdminDomainObject(ORGANIZATION_DN);
	private static final Dn ADMIN_DN = new Dn("erglobalid=123,ou=0,ou=people,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");
	private static final int LIMIT = 10;

	@InjectMocks
	AdminDomainServiceImpl adminDomainService;

	@Mock
	IsimLdapManager isimLdapManager;

	@Mock
	AdminDomainConverter adminDomainConverter;

	@Mock
	ApplicationProperties properties;

	@Test
	void getGlobalIdsFromNonVirtualAdminsFor_orgIdIsNull_OrganizationNotFoundException() {
		assertThrows(OrganizationNotFoundException.class,
				() -> adminDomainService.getGlobalIdsFromNonVirtualAdminsFor(null));
	}

	@Test
	void getGlobalIdsFromNonVirtualAdminsFor_orgIdIsEmpty_OrganizationNotFoundException() {
		assertThrows(OrganizationNotFoundException.class,
				() -> adminDomainService.getGlobalIdsFromNonVirtualAdminsFor(""));
	}

	@Test
	void getGlobalIdsFromNonVirtualAdminsFor_hasAdmin_returnsGlobalId() {
		String globalId = "7797760088491035196";
		String orgId = "123";
		IsimLdapAdminDomain adminDomain = createAdminDomain();
		when(isimLdapManager.getAdminDomainByOrgId(orgId, DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_INCL_ROLES)).thenReturn(Optional.of(adminDomain));

		AdminDomainObject adminDomainObject = createAdminDomainObject(globalId);
		when(adminDomainConverter.convert(adminDomain)).thenReturn(adminDomainObject);

		List<String> globalIdsFromNonVirtualAdminsFor = adminDomainService.getGlobalIdsFromNonVirtualAdminsFor(orgId);

		assertThat(globalIdsFromNonVirtualAdminsFor).contains(globalId);
	}

	@Test
	void getGlobalIdsFromNonVirtualAdminsFor_1VirtualAdminAnd1Normal_onlyReturnsGlobalIdOfNormalAdmin() {
		String globalIdReal = "7797760088491035196";
		String globalIdVirtual = "789";
		String orgId = "123";
		IsimLdapAdminDomain adminDomain = createAdminDomain();
		when(isimLdapManager.getAdminDomainByOrgId(orgId, DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_INCL_ROLES)).thenReturn(Optional.of(adminDomain));

		AdminDomainObject adminDomainObject = createAdminDomainObjectWith2Admins(globalIdVirtual, globalIdReal);
		when(adminDomainConverter.convert(adminDomain)).thenReturn(adminDomainObject);

		List<String> globalIdsFromNonVirtualAdminsFor = adminDomainService.getGlobalIdsFromNonVirtualAdminsFor(orgId);

		assertThat(globalIdsFromNonVirtualAdminsFor).containsOnly(globalIdReal);
	}

	@Test
	void findByIkp() {
		Optional<AdminDomainObject> adminDomainObject = this.adminDomainService.findAdminDomainByIkp(IKP);
		assertThat(adminDomainObject).isNotNull();
	}

	@Test
	void findPersonsFromOrganization() {
		Optional<AdminDomainObject> adminDomainObject = this.adminDomainService.findAdminDomainByDn(ORGANIZATION_DN);
		assertThat(adminDomainObject).isNotNull();
	}

	@Test
	void searchAdminDomain() {
		when(this.properties.getDaoMaxRecords()).thenReturn(200);

		AdminDomainSearch adminDomainSearch = new AdminDomainSearch();
		adminDomainSearch.setName(ADMIN_NAME);
		adminDomainSearch.setStreet(ADMIN_STREET);
		adminDomainSearch.setPostalcode(ADMIN_POSTALCODE);
		adminDomainSearch.setCity(ADMIN_CITY);
		adminDomainSearch.setIkp(ADMIN_IKP);
		adminDomainSearch.setIkpEnd(ADMIN_IKP_END);
		adminDomainSearch.setKboNummer(ADMIN_KBONUMMER);
		adminDomainSearch.setOe(ADMIN_OE);
		adminDomainSearch.setLimit(LIMIT);
		LdapFilter filter = adminDomainSearch.ldapFilter();

		log.debug("filter: {}", adminDomainSearch.getSearchFilter());

		assertThat(adminDomainSearch.hasSearchCriteria()).isTrue();
		VestigingenZoekResultaat adminDomains = adminDomainService.findAdminDomainBySearchCriteria(adminDomainSearch);
		verify(isimLdapManager).getAdminDomainsByFilter(filter, DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_EXCL_ROLES, LIMIT);
		assertThat(adminDomains).isNotNull();
		assertThat(adminDomains.vestigingen()).isNotNull();
	}

	@Test
	void searchAdminDomainWithoutCriteria() {
		when(this.properties.getDaoMaxRecords()).thenReturn(200);

		AdminDomainSearch adminDomainSearch = new AdminDomainSearch();
		adminDomainSearch.setName(null);
		adminDomainSearch.setStreet(null);
		adminDomainSearch.setPostalcode(null);
		adminDomainSearch.setCity(null);
		adminDomainSearch.setIkp(null);
		adminDomainSearch.setIkpEnd(null);
		adminDomainSearch.setKboNummer(null);
		adminDomainSearch.setOe(null);

		log.debug("filter: {}", adminDomainSearch.getSearchFilter());

		VestigingenZoekResultaat adminDomains = adminDomainService.findAdminDomainBySearchCriteria(adminDomainSearch);
		assertThat(adminDomains).isNotNull();
		assertThat(adminDomains.vestigingen()).isNotNull();
	}

	@Test
	void getAdminDomainsByFilter() {
		initGetAdminDomainsByFilter();

		List<AdminDomainObject> adminDomainObjects = adminDomainService.findAdminDomainsForAdministrator(ADMIN_DN);
		assertThat(adminDomainObjects).isNotNull().hasSize(2);
	}

	@Test
	void getAdminDomainsByFilterWithAttributeList() {
		initGetAdminDomainsByFilter();

		List<String> attributes = Arrays.asList(IsimAttributeNames.ATTR_GLOBALID);

		List<AdminDomainObject> adminDomainObjects = adminDomainService.findAdminDomainsForAdministrator(ADMIN_DN, attributes);
		assertThat(adminDomainObjects).isNotNull().hasSize(2);
	}

	private void initGetAdminDomainsByFilter() {
		IsimLdapAdminDomain isimLdapAdminDomain = createAdminDomain();
		List<IsimLdapAdminDomain> isimAdmindomains = Arrays.asList(isimLdapAdminDomain);
		when(isimLdapManager.getAdminDomainsByFilter(any(LdapFilter.class), anyList())).thenReturn(isimAdmindomains);
		when(adminDomainConverter.convert(isimLdapAdminDomain, false)).thenReturn(ADMIN_DOMAIN_OBJECT);
	}

	private AdminDomainObject createAdminDomainObjectWith2Admins(String globalIdVirtual, String globalIdReal) {
		PersonObject personObjectVirtual = createPersonObject(globalIdVirtual, true);
		PersonObject personObjectReal = createPersonObject(globalIdReal, false);
		AdminDomainObject adminDomainObject = new AdminDomainObject();
		adminDomainObject.addAdministrator(personObjectVirtual);
		adminDomainObject.addAdministrator(personObjectReal);

		return adminDomainObject;
	}

	private IsimLdapAdminDomain createAdminDomain() {
		IsimLdapAdminDomain adminDomain = mock(IsimLdapAdminDomain.class);
		lenient().when(adminDomain.getDn()).thenReturn(Dn.of("erglobalid=456,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be"));
		return adminDomain;
	}

	private AdminDomainObject createAdminDomainObject(String globalId) {
		PersonObject personObject = createPersonObject(globalId, false);
		AdminDomainObject adminDomainObject = new AdminDomainObject();
		adminDomainObject.addAdministrator(personObject);
		return adminDomainObject;
	}

	private PersonObject createPersonObject(String globalId, boolean isVirtual) {
		Dn dnObject = Dn.of("erglobalid=" + globalId + ",ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");
		PersonObject personObject = new PersonObject();
		personObject.setVirtualAccount(isVirtual);
		personObject.setDn(dnObject);
		return personObject;
	}
}