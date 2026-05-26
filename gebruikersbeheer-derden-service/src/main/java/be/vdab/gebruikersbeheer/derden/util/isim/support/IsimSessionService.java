package be.vdab.gebruikersbeheer.derden.util.isim.support;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.exception.ITIMContextManagerException;
import be.vdab.gebruikersbeheer.derden.monitoring.MonitoringService;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWebservices;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.client.ResettableWSSessionService;
import be.vdab.tokenutil.domain.JWTToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IsimSessionService {

	private final ApplicationProperties properties;
	private final ResettableWSSessionService resettableWSSessionService;
	private final IsimWebservices isimWebservices;
	private final MonitoringService monitoringService;

	public IsimWsSession getSession(JWTToken jwt) {
		try {
			String ltpaToken = this.isimWebservices.getLtpaFromJWT(jwt.getToken());
			if (StringUtils.isEmpty(ltpaToken)) {
				log.error("Error creating ltpatoken from jwt: {}", jwt.getToken());
			}
			IsimWsSession session = this.isimWebservices.getISIMSessionByLTPAToken(ltpaToken);
			monitoringService.sessionCreated();
			return session;
		} catch (Exception e) {
			log.error("Fout tijdens inloggen");
			throw new ITIMContextManagerException("Error occurred while trying to create a login context for user", e);
		}
	}

	public IsimWsSession getAdminSession() {
		try {
			// via username / password
			log.debug("No JWT, using {} to create ISIM session", this.properties.getIsimUser());
			String username = this.properties.getIsimUser();
			IsimWsSession session = resettableWSSessionService.login(username, this.properties.getIsimPassword());
			monitoringService.sessionCreated();
			return session;
		} catch (Exception e) {
			log.error("Fout tijdens inloggen");
			throw new ITIMContextManagerException("Error occurred while trying to create a login context for user", e);
		}
	}
}
