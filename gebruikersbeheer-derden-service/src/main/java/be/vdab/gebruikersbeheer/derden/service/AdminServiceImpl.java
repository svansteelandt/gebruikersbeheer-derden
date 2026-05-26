package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.CacheNames;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.util.LogSanitizer;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsAdminDomain;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

	private final IsimWsClient isimWsClient;
	private final CacheManager cacheManager;
	private final IsimUserContextManager isimUserContextManager;

	@Override
	public void createNewAdmin(AdminDomainObject adminDomain, PersonObject adminPerson) {
		if (log.isDebugEnabled()) {
			log.debug("createAdmin Admindomain: {} distinguishednameAdmin: {}", LogSanitizer.sanitize(adminDomain.getName()), LogSanitizer.sanitize(adminPerson.getUserId()));
		}

		var isimWsSession = isimUserContextManager.getSession();
		IsimWsAdminDomain adminDomainOld = isimWsClient.getAdminDomainByDn(isimWsSession, adminDomain.getDn());
		IsimWsAdminDomain adminDomainNew = adminDomainOld.addAdministrator(adminPerson.getDn());
		isimWsClient.updateAdminDomain(isimWsSession, adminDomainOld, adminDomainNew);

		updateAdminDomainCache(adminDomain, adminPerson, true);
	}

	@Override
	public void deleteAdmin(AdminDomainObject adminDomain, PersonObject adminPerson) {
		if (log.isDebugEnabled()) {
			log.debug("deleteAdmin Admindomain: {} distinguishednameAdmin: {}", LogSanitizer.sanitize(adminDomain.getName()), LogSanitizer.sanitize(adminPerson.getUserId()));
		}

		var isimWsSession = isimUserContextManager.getSession();
		IsimWsAdminDomain adminDomainOld = isimWsClient.getAdminDomainByDn(isimWsSession, adminDomain.getDn());
		IsimWsAdminDomain adminDomainNew = adminDomainOld.deleteAdministrator(adminPerson.getDn());
		isimWsClient.updateAdminDomain(isimWsSession, adminDomainOld, adminDomainNew);

		updateAdminDomainCache(adminDomain, adminPerson, false);
	}

	@Override
	public void deleteFromAdminDomainCache(Object key) {
		var cacheAdminDomain = this.cacheManager.getCache(CacheNames.CACHE_ADMINDOMAIN);
		if (cacheAdminDomain != null) {
			cacheAdminDomain.evict(key);
		}
	}

	private void updateAdminDomainCache(AdminDomainObject adminDomain, PersonObject adminPerson, boolean insert) {
		if (insert) {
			adminDomain.addAdministrator(adminPerson);
		} else {
			adminDomain.deleteAdministrator(adminPerson);
		}

		List<PersonObject> admins = adminDomain.getAdministrators().stream().filter(p -> !p.isVirtualAccount()).collect(Collectors.toList());
		List<RoleObject> roles = adminDomain.getRoles();

		int adminAantal = admins.size();
		getAdminRole(roles).ifPresent(adminRole -> {
			if (adminAantal == 2) {
				adminRole.setAvailable(false);
			} else if (adminAantal < 2) {
				adminRole.setAvailable(true);
			}
		});

		var cacheAdminDomain = this.cacheManager.getCache(CacheNames.CACHE_ADMINDOMAIN);

		if (cacheAdminDomain != null) {
			cacheAdminDomain.put(adminDomain.getDn(), adminDomain);
		}
	}

	private Optional<RoleObject> getAdminRole(List<RoleObject> roles) {
		return roles.stream().filter(RoleObject::isAdminRole).findFirst();
	}
}
