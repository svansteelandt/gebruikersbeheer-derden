package be.vdab.gebruikersbeheer.derden.security;

import be.vdab.gebruikersbeheer.derden.domain.IsimUserData;
import be.vdab.gebruikersbeheer.derden.monitoring.MonitoringService;
import be.vdab.gebruikersbeheer.derden.util.isim.support.ContextManager;
import be.vdab.gebruikersbeheer.derden.util.isim.support.IsimSessionService;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.iam.oidc.authentication.principal.ClientPrincipal;
import be.vdab.tokenutil.domain.JWTToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class IsimUserContextManager implements ContextManager {

	private static final ThreadLocal<IsimWsSession> sessionHolder = new NamedThreadLocal<>("Itim Session");
	private static final ThreadLocal<IsimWsSession> adminSessionHolder = new NamedThreadLocal<>("Session for technical user");
	private static final ThreadLocal<JWTToken> jwtHolder = new NamedThreadLocal<>("JWT");

	private final IsimSessionService isimSessionService;
	private final MonitoringService monitoringService;

	@NonNull
	public IsimWsSession getSession() {
		if (adminSessionIsActive()) {
			return getAdminSession();
		}
		if (canCreateAuthenticatedUserSession()) {
			return getAuthenticatedUserSession();
		}
		return getAdminSession();
	}

	private IsimWsSession getAuthenticatedUserSession() {
		if (sessionHolder.get() == null) {
			setIsimSessionForAuthenticatedUser();
		}
		return sessionHolder.get();
	}

	private IsimWsSession getAdminSession() {
		if (adminSessionHolder.get() == null) {
			adminSessionHolder.set(isimSessionService.getAdminSession());
			if (!isClientCredentialUsed()) {
				log.warn("Started isim session for user without JWT token.");
			}
		}
		return adminSessionHolder.get();
	}

	private boolean isClientCredentialUsed() {
		return Optional.ofNullable(SecurityContextHolder.getContext())
				.map(SecurityContext::getAuthentication)
				.filter(authentication -> authentication instanceof ClientPrincipal)
				.isPresent();
	}

	private boolean adminSessionIsActive() {
		return adminSessionHolder.get() != null;
	}

	@Override
	public void startPrivilegedSession() {
		if (!adminSessionIsActive()) {
			adminSessionHolder.set(isimSessionService.getAdminSession());
		}
	}

	@Override
	public void endPrivilegedSession() {
		if (adminSessionIsActive() && canCreateAuthenticatedUserSession()) {
			adminSessionHolder.remove();
		}
	}

	public void setJwt(JWTToken jwt) {
		if (jwt == null) {
			jwtHolder.remove();
		} else {
			jwtHolder.set(jwt);
		}
	}

	@Override
	public void clearSessions() {
		IsimWsSession session = sessionHolder.get();
		if (session != null) {
			session.close();
			this.monitoringService.sessionDestroyed();
		}
		sessionHolder.remove();

		IsimWsSession adminSession = adminSessionHolder.get();
		if (adminSession != null) {
			adminSession.close();
			this.monitoringService.sessionDestroyed();
		}
		adminSessionHolder.remove();

		jwtHolder.remove();

		IsimUserContextHolder.clearContext();
	}

	@Override
	public void init() {
		if (adminSessionHolder.get() != null) {
			log.warn("Admin session holder was not empty when initializing it");
			adminSessionHolder.get().close();
			adminSessionHolder.remove();
			this.monitoringService.sessionDestroyed();
		}
		if (sessionHolder.get() != null) {
			log.warn("Session holder was not empty when initializing it");
			sessionHolder.get().close();
			adminSessionHolder.remove();
			this.monitoringService.sessionDestroyed();
		}
		if (jwtHolder.get() != null) {
			log.warn("JWT holder was not empty when initializing it");
			jwtHolder.remove();
		}
		if (IsimUserContextHolder.getContext() != null) {
			log.warn("Isim User Data holder was not empty when initializing it");
			IsimUserContextHolder.clearContext();
		}
	}

	public void setContext(IsimUserData isimUserData) {
		IsimUserContextHolder.setContext(isimUserData);
	}

	boolean canCreateAuthenticatedUserSession() {
		return jwtHolder.get() != null;
	}

	private void setIsimSessionForAuthenticatedUser() {
		if (canCreateAuthenticatedUserSession()) {
			sessionHolder.set(isimSessionService.getSession(jwtHolder.get()));
		}
	}
}
