package be.vdab.gebruikersbeheer.derden.security;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.IsimUserData;
import be.vdab.gebruikersbeheer.derden.monitoring.MonitoringService;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.util.isim.support.IsimSessionService;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.iam.oidc.authentication.SecurityDomain;
import be.vdab.iam.oidc.authentication.principal.VdabPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityExpressionsTest {

	@Mock
	VdabPrincipal vdabPrincipal;

	IsimUserContextManager isimUserContextManager;

	@Mock
	AdminDomainService adminDomainService;

	SecurityExpressions securityExpressions;

	@BeforeEach
	void init() {
		isimUserContextManager = new IsimUserContextManager(mock(IsimSessionService.class), mock(MonitoringService.class));
		securityExpressions = new SecurityExpressions(adminDomainService);
	}

	@AfterEach
	void afterEach() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void canAccessOrganizationWithGlobalIdAsInternalUser() {
		when(vdabPrincipal.getSecurityDomain()).thenReturn(SecurityDomain.INTERN);

		setupLoggedInUserMocks();

		assertThat(securityExpressions.canAccessOrganizationWithGlobalId("1234")).isTrue();
	}

	@Test
	void canAccessOrganizationWithGlobalIdAsDerdeUser() {
		when(vdabPrincipal.getSecurityDomain()).thenReturn(SecurityDomain.DERDE);

		AdminDomainObject admin1 = new AdminDomainObject(new Dn("erglobalid=1234"));
		admin1.setGlobalId("1234");

		AdminDomainObject admin2 = new AdminDomainObject(new Dn("erglobalid=5678"));
		admin2.setGlobalId("5678");

		when(adminDomainService.findAdminDomainsForAdministrator(any(Dn.class), anyList())).thenReturn(List.of(admin1, admin2));

		setupLoggedInUserMocks();

		assertThat(securityExpressions.canAccessOrganizationWithGlobalId("1234")).isTrue();
		assertThat(securityExpressions.canAccessOrganizationWithGlobalId("9876")).isFalse();
	}

	@Test
	void canAccessOrganizationWithGlobalIdAsBurger() {
		when(vdabPrincipal.getSecurityDomain()).thenReturn(SecurityDomain.BURGER);

		setupLoggedInUserMocks();

		assertThat(securityExpressions.canAccessOrganizationWithGlobalId("1234")).isFalse();
	}

	@Test
	@DisplayName("""
			GIVEN user with profile 'vdabintern'
			THEN RETURN user is 'intern' user
			""")
	void isIntern() {
		setupLoggedInUserMocks();
		when(vdabPrincipal.getSecurityDomain()).thenReturn(SecurityDomain.INTERN);
		assertThat(securityExpressions.isIntern()).isTrue();
		assertThat(securityExpressions.isIntern(SecurityContextHolder.getContext().getAuthentication())).isTrue();

		when(vdabPrincipal.getSecurityDomain()).thenReturn(SecurityDomain.DERDE);
		assertThat(securityExpressions.isIntern()).isFalse();
		assertThat(securityExpressions.isIntern(SecurityContextHolder.getContext().getAuthentication())).isFalse();
	}

	@Test
		//@WithJwtPrincipal(username = "JOSKE", roles = RoleNames.ROL_BEHEERDERS_DERDEN)
	void isDerde() {
		setupLoggedInUserMocks();
		when(vdabPrincipal.getSecurityDomain()).thenReturn(SecurityDomain.DERDE);
		assertThat(securityExpressions.isDerde(SecurityContextHolder.getContext().getAuthentication())).isTrue();

		when(vdabPrincipal.getSecurityDomain()).thenReturn(SecurityDomain.INTERN);
		assertThat(securityExpressions.isDerde(SecurityContextHolder.getContext().getAuthentication())).isFalse();
	}

	@Test
	void magEditerenIndienRolBeheerdersDerden() {
		setupLoggedInUserMocks();
		when(vdabPrincipal.hasAnyRole(RoleNames.ROL_BEHEERDERS_DERDEN, RoleNames.ROL_BEHEERDERS_DERDEN_ZONDER_RRN)).thenReturn(true);
		assertThat(securityExpressions.isBeheerderDerden()).isTrue();
	}

	@Test
	void magEditerenIndienRolBeheerdersDerdenZonderRrnr() {
		setupLoggedInUserMocks();
		when(vdabPrincipal.hasAnyRole(RoleNames.ROL_BEHEERDERS_DERDEN, RoleNames.ROL_BEHEERDERS_DERDEN_ZONDER_RRN)).thenReturn(true);
		assertThat(securityExpressions.isBeheerderDerden()).isTrue();
	}

	@Test
	void magNietEditerenIndienGeenVanBenodigdeRollen() {
		setupLoggedInUserMocks();
		assertThat(securityExpressions.isBeheerderDerden()).isFalse();
	}

	@Test
	void beheerderExtranetCanChangeRights() {
		setupLoggedInUserMocks();
		when(vdabPrincipal.hasAnyRole(RoleNames.ROL_BEHEERDERS_EXTRANET, RoleNames.ROL_EXTRANET_EXPERT, RoleNames.ROL_KIOSK_ADMIN)).thenReturn(true);
		assertThat(securityExpressions.canChangeRights()).isTrue();
	}

	@Test
	void extranetExpertCanChangeRights() {
		setupLoggedInUserMocks();
		when(vdabPrincipal.hasAnyRole(RoleNames.ROL_BEHEERDERS_EXTRANET, RoleNames.ROL_EXTRANET_EXPERT, RoleNames.ROL_KIOSK_ADMIN)).thenReturn(true);
		assertThat(securityExpressions.canChangeRights()).isTrue();
	}

	@Test
	void kioskAdminCanChangeRights() {
		setupLoggedInUserMocks();
		when(vdabPrincipal.hasAnyRole(RoleNames.ROL_BEHEERDERS_EXTRANET, RoleNames.ROL_EXTRANET_EXPERT, RoleNames.ROL_KIOSK_ADMIN)).thenReturn(true);
		assertThat(securityExpressions.canChangeRights()).isTrue();
	}

	private void setupLoggedInUserMocks() {
		Authentication authentication = mock(Authentication.class);

		when(authentication.getPrincipal()).thenReturn(vdabPrincipal);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		isimUserContextManager.setContext(IsimUserData.builder().personDn(new Dn("erglobalid=1234")).build());
	}
}