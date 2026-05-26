package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.core.CodesPort;
import be.vdab.gebruikersbeheer.derden.domain.IsimUserData;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.monitoring.MonitoringService;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.util.isim.support.IsimSessionService;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.iam.oidc.authentication.LegacyUser;
import be.vdab.iam.oidc.authentication.SecurityDomain;
import be.vdab.iam.oidc.authentication.principal.LegacyInternePrincipal;
import be.vdab.iam.oidc.authentication.principal.VdabPrincipal;
import be.vdab.iam.oidc.authorization.GrantedRole;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAdditionalRolesTest {

	private static final String EXTRANET_ROLE_2 = "AR-EXTRANET-role-2";
	private static final String EXTRANET_ROLE_3 = "AR-EXTRANET-role-3";
	private static final String EXTRANET_ROLE_1 = "AR-EXTRANET-role-1";
	private static final String RANDOM_ROLE_1 = "AR-RANDOM-role-1";
	private static final String KIOSK_PARTNER_ROLE = "AR-KIOSK-PARTNER";
	private static final String DERDE_INTERN = "DerdeIntern";
	private static final String ERGLOBALID_1 = "1234";
	private static final String ERGLOBALID_2 = "12345";
	private static final String ERGLOBALID_3 = "123456";

	@InjectMocks
	AdminDomainServiceImpl adminDomainService;

	@Mock
	RoleService roleService;

	@Mock
	CodesPort codesPort;

	@Mock
	IsimSessionService isimSessionService;

	@Mock
	MonitoringService monitoringService;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	IsimUserContextManager isimUserContextManager;

	@BeforeEach
	void setUp() {
		isimUserContextManager = new IsimUserContextManager(isimSessionService, monitoringService);
	}

	@AfterEach
	void cleanUp() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void returnsOnlyExtranetAndKioskPartnerRoles() {
		login(RoleNames.ROL_KIOSK_ADMIN, RoleNames.ROL_EXTRANET_EXPERT);

		List<RoleObject> staticRoles = new ArrayList<>();
		RoleObject role1 = createRole(EXTRANET_ROLE_1, ERGLOBALID_1, DERDE_INTERN);
		RoleObject role2 = createRole(EXTRANET_ROLE_2, ERGLOBALID_2, DERDE_INTERN);
		RoleObject role3 = createRole(KIOSK_PARTNER_ROLE, ERGLOBALID_2, DERDE_INTERN);
		RoleObject role4 = createRole(RANDOM_ROLE_1, ERGLOBALID_3, DERDE_INTERN);
		staticRoles.add(role1);
		staticRoles.add(role2);
		staticRoles.add(role3);
		staticRoles.add(role4);

		when(roleService.findRoles()).thenReturn(staticRoles);
		when(codesPort.getAdditionalTimRollen()).thenReturn(Collections.emptyList());

		List<RoleObject> additionalRoles = adminDomainService.getAdditionalRoles(Collections.emptyList());
		assertThat(additionalRoles).hasSize(3);
		assertThat(additionalRoles.stream().map(RoleObject::getRoleName)).containsOnly(EXTRANET_ROLE_1, EXTRANET_ROLE_2, KIOSK_PARTNER_ROLE);
	}

	@Test
	void doesNotReturnIsimRolesFromExtranetRekader() {
		login(RoleNames.ROL_KIOSK_ADMIN, RoleNames.ROL_EXTRANET_EXPERT);

		List<RoleObject> staticRoles = new ArrayList<>();
		RoleObject role1 = createRole(EXTRANET_ROLE_1, ERGLOBALID_1, DERDE_INTERN);
		RoleObject role2 = createRole(EXTRANET_ROLE_2, ERGLOBALID_2, DERDE_INTERN);
		staticRoles.add(role1);
		staticRoles.add(role2);

		List<String> refkaderRoles = new ArrayList<>();
		refkaderRoles.add(EXTRANET_ROLE_2);

		when(roleService.findRoles()).thenReturn(staticRoles);
		when(codesPort.getAdditionalTimRollen()).thenReturn(refkaderRoles);

		List<RoleObject> result = adminDomainService.getAdditionalRoles(Collections.emptyList());

		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getRoleName()).isEqualTo(EXTRANET_ROLE_1);
	}

	@Test
	void onlyReturnsRolesThatAreDerdeIntern() {
		login(RoleNames.ROL_KIOSK_ADMIN, RoleNames.ROL_EXTRANET_EXPERT);

		List<RoleObject> staticRoles = new ArrayList<>();
		RoleObject role1 = createRole(EXTRANET_ROLE_1, ERGLOBALID_1, "DerdeExtern");
		RoleObject role2 = createRole(EXTRANET_ROLE_2, ERGLOBALID_2, DERDE_INTERN);
		staticRoles.add(role1);
		staticRoles.add(role2);

		when(roleService.findRoles()).thenReturn(staticRoles);
		when(codesPort.getAdditionalTimRollen()).thenReturn(Collections.emptyList());

		List<RoleObject> result = adminDomainService.getAdditionalRoles(Collections.emptyList());

		assertThat(result).hasSize(1);
		assertThat(result.getFirst().getRoleName()).isEqualTo(EXTRANET_ROLE_2);
	}

	@Test
	void whenPrincipalHasNoBerheerderRoleAndHasExpertRole_onlyApprovedRolesAreReturned() {
		login(RoleNames.ROL_EXTRANET_EXPERT);

		Dn principalDn = Dn.of("erglobalid=654321,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");

		List<RoleObject> staticRoles = new ArrayList<>();
		RoleObject role1 = createRole(EXTRANET_ROLE_1, ERGLOBALID_1, DERDE_INTERN);
		RoleObject role2 = createRole(EXTRANET_ROLE_2, ERGLOBALID_2, DERDE_INTERN);
		RoleObject role3 = createRole(EXTRANET_ROLE_3, ERGLOBALID_3, DERDE_INTERN);
		role1.addOwner(principalDn);
		role1.setNeedsApproval(true);
		role2.setNeedsApproval(true);
		role3.setNeedsApproval(false);

		staticRoles.add(role1);
		staticRoles.add(role2);
		staticRoles.add(role3);

		when(roleService.findRoles()).thenReturn(staticRoles);
		when(codesPort.getAdditionalTimRollen()).thenReturn(Collections.emptyList());

		IsimUserData userData = mock(IsimUserData.class);
		when(userData.getPersonDn()).thenReturn(principalDn);
		isimUserContextManager.setContext(userData);

		List<RoleObject> result = adminDomainService.getAdditionalRoles(Collections.emptyList());

		assertThat(result).hasSize(2);
		assertThat(result.stream()
				.map(RoleObject::getRoleName))
				.containsOnly(EXTRANET_ROLE_1, EXTRANET_ROLE_3);
	}

	@Test
	void kioskAdminPrincipalOnlyGetsKioskRoles() {
		login(RoleNames.ROL_KIOSK_ADMIN);

		PersonObject principal = new PersonObject();
		Dn principalDn = Dn.of("erglobalid=654321,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");
		principal.setDn(principalDn);

		List<RoleObject> staticRoles = new ArrayList<>();
		RoleObject role1 = createRole(EXTRANET_ROLE_1, ERGLOBALID_1, DERDE_INTERN);
		RoleObject role2 = createRole(EXTRANET_ROLE_2, ERGLOBALID_2, DERDE_INTERN);
		RoleObject role3 = createRole(KIOSK_PARTNER_ROLE, ERGLOBALID_3, DERDE_INTERN);
		role1.addOwner(principalDn);
		role1.setNeedsApproval(false);
		role2.setNeedsApproval(false);
		role3.setNeedsApproval(false);

		staticRoles.add(role1);
		staticRoles.add(role2);
		staticRoles.add(role3);

		when(roleService.findRoles()).thenReturn(staticRoles);
		when(codesPort.getAdditionalTimRollen()).thenReturn(Collections.emptyList());

		List<RoleObject> result = adminDomainService.getAdditionalRoles(Collections.emptyList());

		assertThat(result).hasSize(1);
		assertThat(result.stream()
				.map(RoleObject::getRoleName))
				.containsOnly(KIOSK_PARTNER_ROLE);
	}

	@Test
	void extranetBeheerderPrincipalGetsAllRolesExceptKiosk() {
		login(RoleNames.ROL_BEHEERDERS_EXTRANET);

		PersonObject principal = new PersonObject();
		Dn principalDn = Dn.of("erglobalid=654321,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be");
		principal.setDn(principalDn);

		List<RoleObject> staticRoles = new ArrayList<>();
		RoleObject role1 = createRole(EXTRANET_ROLE_1, ERGLOBALID_1, DERDE_INTERN);
		RoleObject role2 = createRole(EXTRANET_ROLE_2, ERGLOBALID_2, DERDE_INTERN);
		RoleObject role3 = createRole(EXTRANET_ROLE_3, ERGLOBALID_2, DERDE_INTERN);
		RoleObject role4 = createRole(KIOSK_PARTNER_ROLE, ERGLOBALID_3, DERDE_INTERN);
		role1.addOwner(principalDn);
		role1.setNeedsApproval(true);
		role2.setNeedsApproval(true);
		role3.setNeedsApproval(false);
		role4.setNeedsApproval(false);

		staticRoles.add(role1);
		staticRoles.add(role2);
		staticRoles.add(role3);
		staticRoles.add(role4);

		when(roleService.findRoles()).thenReturn(staticRoles);
		when(codesPort.getAdditionalTimRollen()).thenReturn(Collections.emptyList());

		List<RoleObject> result = adminDomainService.getAdditionalRoles(Collections.emptyList());

		assertThat(result).hasSize(3);
		assertThat(result.stream()
				.map(RoleObject::getRoleName))
				.containsOnly(EXTRANET_ROLE_1, EXTRANET_ROLE_2, EXTRANET_ROLE_3);
	}

	@Test
	void onlyRolesThatAreInTheInputHaveHasRoleTrue() {
		login(RoleNames.ROL_KIOSK_ADMIN, RoleNames.ROL_EXTRANET_EXPERT);

		List<RoleObject> staticRoles = new ArrayList<>();
		RoleObject role1 = createRole(EXTRANET_ROLE_1, ERGLOBALID_1, DERDE_INTERN);
		RoleObject role2 = createRole(EXTRANET_ROLE_2, ERGLOBALID_2, DERDE_INTERN);
		RoleObject role3 = createRole("AR-EXTRANET-role-4", "1234567", DERDE_INTERN);
		staticRoles.add(role1);
		staticRoles.add(role2);
		staticRoles.add(role3);

		when(roleService.findRoles()).thenReturn(staticRoles);
		when(codesPort.getAdditionalTimRollen()).thenReturn(Collections.emptyList());

		List<RoleObject> inputRoles = new ArrayList<>();
		RoleObject role4 = createRole(EXTRANET_ROLE_1, ERGLOBALID_1);
		RoleObject role5 = createRole(EXTRANET_ROLE_2, ERGLOBALID_2);
		RoleObject role6 = createRole(EXTRANET_ROLE_3, ERGLOBALID_3);

		inputRoles.add(role4);
		inputRoles.add(role5);
		inputRoles.add(role6);

		List<RoleObject> result = adminDomainService.getAdditionalRoles(inputRoles);
		assertThat(result).hasSize(3);
		assertThat(result.stream()
				.filter(RoleObject::getHasRole)
				.map(RoleObject::getRoleName))
				.containsOnly(EXTRANET_ROLE_1, EXTRANET_ROLE_2);
	}

	@NotNull
	private RoleObject createRole(String roleName, String erglobalid) {
		RoleObject role = new RoleObject();
		role.setRoleName(roleName);
		role.setDn(Dn.of("erglobalid=" + erglobalid + ",ou=orgChart,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be"));
		return role;
	}

	@NotNull
	private RoleObject createRole(String roleName, String erglobalid, String tag) {
		RoleObject role = createRole(roleName, erglobalid);
		role.addTag(tag);
		return role;
	}

	private void login(String... roles) {
		SecurityContextHolder.setContext(securityContext);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(getVdabPrincipal(roles));
	}

	private VdabPrincipal getVdabPrincipal(String... roles) {
		LegacyUser legacyUser = new LegacyUser(
				"USERNAME",
				SecurityDomain.INTERN,
				null,
				"Patat",
				"Jef",
				"Jef Patat",
				null,
				null,
				null,
				Arrays.asList(roles),
				null,
				null,
				null
		);

		var grantedRoles = legacyUser.rollen().stream()
				.map(r -> new GrantedRole(r, ""))
				.map(GrantedAuthority.class::cast)
				.toList();

		return new LegacyInternePrincipal(legacyUser, grantedRoles);
	}
}
