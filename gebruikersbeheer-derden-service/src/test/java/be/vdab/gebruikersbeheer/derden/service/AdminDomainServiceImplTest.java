package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsAdminDomain;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDomainServiceImplTest {
	@InjectMocks
	private AdminDomainServiceImpl adminDomainService;

	@Mock
	private AdminDomainObject adminDomainObject;

	@Mock
	private IsimWsClient isimWsClient;
	@Mock
	private AdminService adminService;
	@Mock
	private IsimWsSession isimWsSession;
	@Mock
	private IsimWsAdminDomain isimWsAdminDomain;

	@Mock
	private IsimUserContextManager isimUserContextManager;

	@Test
	void addAndRemoveRolesClearesCache() {
		Dn dn = new Dn("dn");
		when(adminDomainObject.getDn()).thenReturn(dn);
		when(isimUserContextManager.getSession()).thenReturn(isimWsSession);
		when(isimWsClient.getAdminDomainByDn(isimWsSession, adminDomainObject.getDn())).thenReturn(isimWsAdminDomain);
		adminDomainService.addAndRemoveRoles(adminDomainObject, Collections.emptyList(), Collections.emptyList());
		verify(adminService).deleteFromAdminDomainCache(dn);
	}
}
