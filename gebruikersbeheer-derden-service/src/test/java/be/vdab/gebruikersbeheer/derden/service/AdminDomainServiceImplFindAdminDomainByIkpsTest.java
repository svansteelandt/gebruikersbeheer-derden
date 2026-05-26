package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.converter.AdminDomainConverter;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapAdminDomain;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.LdapFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames.DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_EXCL_ROLES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDomainServiceImplFindAdminDomainByIkpsTest {

	private static final String IKP_1 = "103920000";
	private static final String IKP_2 = "104520001";
	private static final LdapFilter EXPECTED_LDAP_QUERY = LdapFilter.orFilter(
			LdapFilter.equalityFilter(IsimAttributeNames.ATTR_IKP, IKP_1),
			LdapFilter.equalityFilter(IsimAttributeNames.ATTR_IKP, IKP_2)
	);

	@InjectMocks
	private AdminDomainServiceImpl adminDomainService;

	@Mock
	private IsimLdapManager isimLdapManager;

	@Mock
	private AdminDomainConverter adminDomainConverter;

	@Mock
	private IsimLdapAdminDomain adminDomain1, adminDomain2;

	@Mock
	private AdminDomainObject adminDomainObject1, adminDomainObject2;

	@Test
	void usesCorrectQuery() {
		List<IsimLdapAdminDomain> adminDomains = Arrays.asList(adminDomain1, adminDomain2);
		List<AdminDomainObject> adminDomainObjects = Arrays.asList(adminDomainObject1, adminDomainObject2);
		when(isimLdapManager.getAdminDomainsByFilter(EXPECTED_LDAP_QUERY, DEFAULT_ADMIN_DOMAIN_ATTRIBUTES_EXCL_ROLES)).thenReturn(adminDomains);
		when(adminDomainConverter.convertList(adminDomains)).thenReturn(adminDomainObjects);

		List<AdminDomainObject> result = adminDomainService.findAdminDomainByIkps(Arrays.asList(IKP_1, IKP_2));
		assertThat(result).isEqualTo(adminDomainObjects);
	}
}
