package be.vdab.gebruikersbeheer.derden.extern.controller.v1;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.IsimUserData;
import be.vdab.gebruikersbeheer.derden.monitoring.MonitoringService;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.CsvExportService;
import be.vdab.gebruikersbeheer.derden.test.TestUtil;
import be.vdab.gebruikersbeheer.derden.util.isim.support.IsimSessionService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.exception.OrganizationNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternOrganizationControllerTest {

	private static final String ORGANIZATION_GLOBAL_ID = "123456";
	private static final Dn ORGANIZATION_DN = TestUtil.isimDn(ORGANIZATION_GLOBAL_ID);
	private static final Ikp IKP = Ikp.of(123456000L);
	private static final Dn DN = Dn.of("cn=some,ou=dn");

	@InjectMocks
	private ExternOrganizationController controller;

	@Mock
	private AdminDomainService adminDomainService;
	@Mock
	private ApplicationProperties applicationProperties;
	@Mock
	private CsvExportService csvExportService;
	@Mock
	IsimSessionService isimSessionService;
	@Mock
	MonitoringService monitoringService;

	@Mock
	private Model model;
	@Mock
	private AdminDomainObject adminDomain, adminDomain2;
	@Mock
	private HttpServletResponse response;

	private IsimUserContextManager isimUserContextManager;

	@BeforeEach
	void setUp() {
		isimUserContextManager = new IsimUserContextManager(isimSessionService, monitoringService);

		lenient().when(applicationProperties.createAdminDomainDn(ORGANIZATION_GLOBAL_ID)).thenReturn(ORGANIZATION_DN);

		lenient().when(adminDomain.getDn()).thenReturn(ORGANIZATION_DN);
		lenient().when(adminDomain.getIkp()).thenReturn(IKP);

		isimUserContextManager.setContext(IsimUserData.builder().personDn(DN).build());
	}

	@Test
	void overview_oneDomain() {
		when(adminDomainService.findAdminDomainsForAdministrator(DN)).thenReturn(List.of(adminDomain));

		String result = controller.overview(model);
		assertThat(result).isEqualTo("redirect:/extern/organization/" + ORGANIZATION_GLOBAL_ID + "/overview");

		verifyNoInteractions(model);
	}

	@Test
	void overview_multipleDomains() {
		List<AdminDomainObject> adminDomains = List.of(adminDomain, adminDomain2);
		when(adminDomainService.findAdminDomainsForAdministrator(any())).thenReturn(adminDomains);

		String result = controller.overview(model);
		assertThat(result).isEqualTo("/extern/organization/overzicht_main");

		verify(model).addAttribute("adminDomains", adminDomains);
		verifyNoMoreInteractions(model);
	}

	@Test
	void detail() {
		when(adminDomainService.findAdminDomainByDnWithRoles(ORGANIZATION_DN)).thenReturn(Optional.of(adminDomain));

		String result = controller.detail(ORGANIZATION_GLOBAL_ID, model);
		assertThat(result).isEqualTo("redirect:/extern/organization/" + ORGANIZATION_GLOBAL_ID + "/person/overzicht");

		verify(model).addAttribute("admindomain", adminDomain);
		verifyNoMoreInteractions(model);
	}

	@Test
	void detail_organizationNotFound() {
		when(adminDomainService.findAdminDomainByDnWithRoles(ORGANIZATION_DN)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> controller.detail(ORGANIZATION_GLOBAL_ID, model))
				.isInstanceOf(OrganizationNotFoundException.class);

		verifyNoInteractions(model);
	}

	@Test
	void exporteerGebruikers() throws Exception {
		when(adminDomainService.findAdminDomainByDnWithRoles(ORGANIZATION_DN)).thenReturn(Optional.of(adminDomain));
		controller.exporteerGebruikers(ORGANIZATION_GLOBAL_ID, response);

		verify(csvExportService).sendCsv(response, IKP, "export-" + ORGANIZATION_GLOBAL_ID + ".csv");
	}
}