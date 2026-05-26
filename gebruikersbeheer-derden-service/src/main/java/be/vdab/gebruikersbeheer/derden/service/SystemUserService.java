package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.converter.SystemUserConverter;
import be.vdab.gebruikersbeheer.derden.domain.SystemUserObject;
import be.vdab.gebruikersbeheer.derden.exception.ITIMContextManagerException;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.util.GebruikersnaamUtils;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemUserService {

	private final IsimWsClient isimWsClient;
	private final IsimUserContextManager isimContextManager;
	private final SystemUserConverter systemUserConverter;


	//@Cacheable(cacheNames = CacheNames.CACHE_SYSTEMUSERS, unless = "#result == null")
	public Optional<SystemUserObject> findUserByUid(String uid) {
		log.debug("systemUserServiceImpl - getSystemUser {}", uid);

		String username = GebruikersnaamUtils.removeSuffix(uid);
		try {
			IsimWsSession session = isimContextManager.getSession();

			return isimWsClient.getSystemUser(session, username)
					.map(systemUserConverter::convert);
		} catch (Exception e) {
			log.error("fout tijdens inloggen van " + username, e);
			throw new ITIMContextManagerException("Error occurred while trying to create a login context for user " + username, e);
		}
	}
}
