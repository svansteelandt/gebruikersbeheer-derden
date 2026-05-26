package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.converter.PersonDeconverter;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsAdminDomain;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsRequestService;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class PersonCreator {
	private static final int SECONDS_TO_WAIT_FOR_CREATION = 120;

	private final PersonDeconverter personDeconverter;
	private final IsimLdapManager isimLdapManager;
	private final IsimWsClient isimWsClient;
	private final IsimWsRequestService isimWsRequestService;
	private final IsimUserContextManager isimUserContextManager;

	Optional<Dn> createPersonInIsim(AdminDomainObject adminDomainObject, PersonObject personObject) {
		long start = System.currentTimeMillis();

		IsimWsSession isimWsSession = isimUserContextManager.getSession();
		IsimWsAdminDomain adminDomain = isimWsClient.getAdminDomainByDn(isimWsSession, adminDomainObject.getDn());
		isimWsClient.createDerde(isimWsSession, adminDomain, builder -> personDeconverter.convert(personObject, builder))
				.ifPresent(request -> isimWsRequestService.waitUntilFinished(isimWsSession, request));

		Waiter.waitFor(start, SECONDS_TO_WAIT_FOR_CREATION, () -> isimLdapManager.getPersonDnByRrnAndAdminDomainDn(personObject.getNationalNumber(),
				adminDomainObject.getDn()).isPresent());
		return isimLdapManager.getPersonDnByRrnAndAdminDomainDn(personObject.getNationalNumber(), adminDomainObject.getDn());
	}
}
