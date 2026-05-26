package be.vdab.gebruikersbeheer.derden.util;

import be.vdab.iam.oidc.authentication.principal.VdabPrincipal;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

	private static SecurityUtils instance;

	public static SecurityUtils getInstance() {
		return SecurityUtils.instance;
	}

	@PostConstruct
	public void init() {
		SecurityUtils.instance = this;
	}

	public VdabPrincipal getSessionIngelogdeUser() {
		if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null) {
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

			if (principal instanceof VdabPrincipal vdabPrincipal) {
				return vdabPrincipal;
			}
		}

		return null;
	}
}
