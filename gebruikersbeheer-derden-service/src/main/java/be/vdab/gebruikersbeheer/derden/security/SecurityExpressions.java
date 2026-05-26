package be.vdab.gebruikersbeheer.derden.security;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.iam.oidc.authentication.SecurityDomain;
import be.vdab.iam.oidc.authentication.principal.VdabPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component("SecurityExpressions")
public class SecurityExpressions {

	private final AdminDomainService adminDomainService;

	public boolean canChangeRights() {
		return getAangelogdeUser().hasAnyRole(RoleNames.ROL_BEHEERDERS_EXTRANET, RoleNames.ROL_EXTRANET_EXPERT, RoleNames.ROL_KIOSK_ADMIN);
	}

	public boolean isBeheerderDerden() {
		return getAangelogdeUser().hasAnyRole(RoleNames.ROL_BEHEERDERS_DERDEN, RoleNames.ROL_BEHEERDERS_DERDEN_ZONDER_RRN);
	}

	public boolean hasRole(String role) {
		return getAangelogdeUser().hasRole(role);
	}

	public VdabPrincipal getAangelogdeUser() {
		return getAangelogdeUser(SecurityContextHolder.getContext().getAuthentication());
	}

	private VdabPrincipal getAangelogdeUser(Authentication authentication) {
		return (VdabPrincipal) authentication.getPrincipal();
	}

	public boolean canAccessOrganizationWithGlobalId(String globalId) {
		SecurityDomain userDomain = getAangelogdeUser().getSecurityDomain();

		if (userDomain == SecurityDomain.INTERN) {
			return true;
		}
		if (userDomain == SecurityDomain.DERDE) {
			Set<String> globalIds = adminDomainService.findAdminDomainsForAdministrator(
							IsimUserContextHolder.getContext().getPersonDn(),
							List.of(IsimAttributeNames.ATTR_GLOBALID)
					).stream()
					.map(AdminDomainObject::getGlobalId)
					.collect(Collectors.toSet());

			return globalIds.contains(globalId);
		}

		return false;
	}

	public boolean isIntern() {
		return isIntern(SecurityContextHolder.getContext().getAuthentication());
	}

	public boolean isIntern(Authentication authentication) {
		return getAangelogdeUser(authentication).getSecurityDomain() == SecurityDomain.INTERN;
	}

	public boolean isDerde() {
		return isDerde(SecurityContextHolder.getContext().getAuthentication());
	}

	public boolean isDerde(Authentication authentication) {
		return getAangelogdeUser(authentication).getSecurityDomain() == SecurityDomain.DERDE;
	}
}
