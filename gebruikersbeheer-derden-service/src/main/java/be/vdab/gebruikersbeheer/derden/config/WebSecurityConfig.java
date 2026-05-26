package be.vdab.gebruikersbeheer.derden.config;

import be.vdab.gebruikersbeheer.derden.domain.IsimUserData;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.SystemUserObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.security.SameSiteCookieCsrfTokenRepository;
import be.vdab.gebruikersbeheer.derden.security.SecurityExpressions;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.derden.service.SystemUserService;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.iam.oidc.authentication.principal.ClientPrincipal;
import be.vdab.iam.oidc.authentication.principal.VdabPrincipal;
import be.vdab.tokenutil.domain.JWTToken;
import be.vdab.tokenutil.service.JWTEncoder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static org.springframework.security.web.util.matcher.RegexRequestMatcher.regexMatcher;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

	private final JWTEncoder jwtEncoder;
	private final IsimUserContextManager isimUserContextManager;
	private final SameSiteCookieCsrfTokenRepository csrfTokenRepository;
	private final SystemUserService systemUserService;
	private final PersonService personService;

	@Bean
	@Order
	public OncePerRequestFilter myFilter() {
		return new IsimUserFilter();
	}

	@Bean
	protected SecurityFilterChain configure(HttpSecurity http, AuthenticationManager authenticationManager, SecurityExpressions securityExpressions) throws Exception {
		return http
				.csrf(configurer -> configurer
						.ignoringRequestMatchers(this::ignoreCsrf)
						.csrfTokenRepository(csrfTokenRepository))
				.sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.headers(configurer -> configurer.frameOptions(FrameOptionsConfig::disable))
				.authorizeHttpRequests(configurer -> configurer
						.requestMatchers(EndpointRequest.to("info", "health", "prometheus", "refresh")).permitAll()
						.requestMatchers("/v3/api-docs", "/v3/api-docs/**").permitAll()
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers(regexMatcher("/api/internal/gebruikersbeheer/v1/organisaties/.*/clearcache")).permitAll()
						.requestMatchers("/intern/**").access(asAuthorizationManager(securityExpressions::isIntern))
						.requestMatchers("/extern/**").access(asAuthorizationManager(securityExpressions::isDerde))
						.anyRequest().authenticated())
				.build();
	}

	private AuthorizationManager<RequestAuthorizationContext> asAuthorizationManager(Function<Authentication, Boolean> hasAccess) {
		return (authSupplier, context) -> new AuthorizationDecision(hasAccess.apply(authSupplier.get()));
	}

	private String getUsernameFromJWTtokenOrPrincipal(HttpServletRequest request, VdabPrincipal principal) {
		String username = principal.getUsername();

		String jwtHeader = request.getHeader(JWTEncoder.JWT_HEADER);

		if (StringUtils.isNotEmpty(jwtHeader)) {
			JWTToken jwtToken = this.jwtEncoder.getJWTToken(jwtHeader);

			if (jwtToken != null) {
				username = jwtToken.getUsername();
				isimUserContextManager.setJwt(jwtToken);
			}
		}

		return username;
	}

	private void loadIsimUser(String username) {
		PersonObject person = personService.findPersonByGebruikersnaam(username, List.of(IsimAttributeNames.ATTR_VDABUID, IsimAttributeNames.ATTR_RIJKSREGISTERNUMMER));

		IsimUserData isimUserData = IsimUserData.builder().personDn(person.getDn()).hoofdGebruikersnaam(person.getVdabUid()).insz(person.getNationalNumber()).build();

		isimUserData.setIsimAccountDn(systemUserService.findUserByUid(username)
				.map(SystemUserObject::getIsimDn)
				.orElse(null));

		isimUserContextManager.setContext(isimUserData);
	}

	private boolean ignoreCsrf(HttpServletRequest request) {
		return !request.getRequestURI().startsWith("/api/ui");
	}

	private class IsimUserFilter extends OncePerRequestFilter {

		@Override
		protected void doFilterInternal(HttpServletRequest request,
		                                HttpServletResponse response,
		                                FilterChain filterChain) throws ServletException, IOException {
			try {
				isimUserContextManager.init();

				if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof VdabPrincipal principal && !(principal instanceof ClientPrincipal)) {
					String username = getUsernameFromJWTtokenOrPrincipal(request, principal);

					if (StringUtils.isNotEmpty(username)) {
						loadIsimUser(username);
					}
				}

				filterChain.doFilter(request, response);
			} finally {
				isimUserContextManager.clearSessions();
			}
		}
	}
}
