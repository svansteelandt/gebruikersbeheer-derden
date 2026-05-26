package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.CacheNames;
import be.vdab.gebruikersbeheer.derden.converter.RoleConverter;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.util.ValidateUtils;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsRequest;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsRequestService;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

	@InjectMocks
	RoleServiceImpl roleService;

	@Mock
	IsimWsClient isimWsClient;

	@Mock
	IsimWsSession isimWsSession;

	@Mock
	IsimLdapManager isimLdapManager;

	@Mock
	PersonObject personObject;

	@Mock
	IsimLdapPerson isimLdapPerson;

	@Mock
	AdminDomainObject adminDomainObject;

	@Mock
	AdminService adminService;

	@Mock
	ValidateUtils validateUtils;

	ServletRequestAttributes requestAttributes;

	@Mock
	HttpServletRequest httpServletRequest;

	@Mock
	HttpSession httpSession;

	@Mock
	CacheManager cacheManager;

	@Mock
	RoleConverter roleConverter;

	@Mock
	Cache cache;

	@Mock
	IsimWsRequest isimWsRequest;

	@Mock
	IsimWsRequestService isimWsRequestService;

	@Mock
	IsimUserContextManager isimUserContextManager;

	@BeforeEach
	void setup() {
		requestAttributes = new ServletRequestAttributes(httpServletRequest);
	}

	@Test
	void changePersonRoleChangeListForAdminRoleWhenRoleObjectHasRoleAndPersonAlreadyHasRoleThenDoNothing() {
		RoleObject roleObject = new RoleObject();
		roleObject.setDn(new Dn("rdn"));
		roleObject.setAdminRole(true);
		roleObject.setHasRole(true);
		roleObject.setChanged(true);
		RequestContextHolder.setRequestAttributes(requestAttributes);
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(personObject.getDn()).thenReturn(new Dn("dn"));
		when(isimLdapPerson.getRollen()).thenReturn(Set.of(roleObject.getDn()));
		when(validateUtils.maxAantalAdmins()).thenReturn(3);
		when(validateUtils.minAantalAdmins()).thenReturn(0);
		when(isimLdapManager.getPersonByDn(personObject.getDn(), IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));

		roleService.changePersonRoleChangeList(List.of(personObject), roleObject, adminDomainObject);

		verify(adminService, never()).deleteAdmin(adminDomainObject, personObject);
	}

	@Test
	void changePersonRoleChangeListForAdminRoleWhenAdminRoleAndRoleObjectHasRoleAndPersonAlreadyHasRoleThenDoNothing() {
		RoleObject roleObject = new RoleObject();
		roleObject.setDn(new Dn("rdn"));
		roleObject.setAdminRole(true);
		roleObject.setHasRole(true);
		roleObject.setChanged(true);
		RequestContextHolder.setRequestAttributes(requestAttributes);
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(personObject.getDn()).thenReturn(new Dn("dn"));
		when(isimLdapPerson.getRollen()).thenReturn(Set.of(roleObject.getDn()));
		when(validateUtils.maxAantalAdmins()).thenReturn(3);
		when(validateUtils.minAantalAdmins()).thenReturn(0);
		when(isimLdapManager.getPersonByDn(personObject.getDn(), IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));

		roleService.changePersonRoleChangeList(List.of(personObject), roleObject, adminDomainObject);

		verify(adminService, never()).deleteAdmin(adminDomainObject, personObject);
	}

	@Test
	void changePersonRoleChangeListForAdminRoleWhenAdminRoleAndRoleObjectHasRoleAndPersonDoesNotHaveRoleAndMaxAdminsNotReachedThenAdd() {
		RoleObject roleObject = new RoleObject();
		roleObject.setDn(new Dn("rdn"));
		roleObject.setAdminRole(true);
		roleObject.setHasRole(true);
		roleObject.setChanged(true);
		RequestContextHolder.setRequestAttributes(requestAttributes);
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(cacheManager.getCache(CacheNames.CACHE_ROLES)).thenReturn(cache);
		when(cache.get(roleObject.getDn(), RoleObject.class)).thenReturn(roleObject);
		when(personObject.getDn()).thenReturn(new Dn("dn"));
		when(isimLdapPerson.getRollen()).thenReturn(Collections.emptySet());
		when(validateUtils.maxAantalAdmins()).thenReturn(3);
		when(validateUtils.minAantalAdmins()).thenReturn(0);
		when(validateUtils.maxAdminsReached(0)).thenReturn(false);
		when(isimLdapManager.getPersonByDn(personObject.getDn(), IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));
		when(isimWsClient.addRolesToPerson(isimWsSession, personObject.getDn(), List.of(roleObject.getDn()))).thenReturn(Optional.of(isimWsRequest));

		roleService.changePersonRoleChangeList(List.of(personObject), roleObject, adminDomainObject);

		verify(adminService).createNewAdmin(adminDomainObject, personObject);
	}

	@Test
	void changePersonRoleChangeListForAdminRoleWhenAdminRoleAndRoleObjectHasRoleAndPersonDoesNotHaveRoleAndMaxAdminReachedThenDoNotAdd() {
		RoleObject roleObject = new RoleObject();
		roleObject.setDn(new Dn("rdn"));
		roleObject.setAdminRole(true);
		roleObject.setHasRole(true);
		roleObject.setChanged(true);
		RequestContextHolder.setRequestAttributes(requestAttributes);
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(adminDomainObject.getAdministrators()).thenReturn(List.of(personObject));
		when(validateUtils.maxAantalAdmins()).thenReturn(1);
		when(validateUtils.minAantalAdmins()).thenReturn(0);
		roleService.changePersonRoleChangeList(List.of(personObject), roleObject, adminDomainObject);

		verify(adminService, never()).createNewAdmin(adminDomainObject, personObject);
	}

	@Test
	void changePersonRoleChangeListForAdminRoleWhenAndPersonDoesNotAlreadyHaveRolHasRoleThenDeleteAdmin() {
		RoleObject roleObject = new RoleObject();
		roleObject.setAdminRole(true);
		roleObject.setChanged(true);
		RequestContextHolder.setRequestAttributes(requestAttributes);
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(personObject.getDn()).thenReturn(new Dn("dn"));
		when(validateUtils.maxAantalAdmins()).thenReturn(3);
		when(validateUtils.minAantalAdmins()).thenReturn(0);
		when(isimLdapManager.getPersonByDn(personObject.getDn(), IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN)).thenReturn(Optional.of(isimLdapPerson));

		roleService.changePersonRoleChangeList(List.of(personObject), roleObject, adminDomainObject);

		verify(adminService).deleteAdmin(adminDomainObject, personObject);
	}

	@ParameterizedTest
	@MethodSource("blankOrNullStrings")
	void findRolesWithEmptyRoleName(String roleName) {
		Optional<RoleObject> rolObject = roleService.findByRoleName(roleName);

		assertThat(rolObject).isNotPresent();
	}

	private static Stream<String> blankOrNullStrings() {
		return Stream.of("", null);
	}
}