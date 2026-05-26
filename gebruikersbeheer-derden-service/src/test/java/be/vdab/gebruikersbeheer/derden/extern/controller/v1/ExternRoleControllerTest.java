package be.vdab.gebruikersbeheer.derden.extern.controller.v1;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.IsimUserData;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.RoleNotFoundException;
import be.vdab.gebruikersbeheer.derden.extern.view.RoleCommand;
import be.vdab.gebruikersbeheer.derden.monitoring.MonitoringService;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.derden.test.TestUtil;
import be.vdab.gebruikersbeheer.derden.util.isim.support.IsimSessionService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.exception.OrganizationNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternRoleControllerTest {

	private static final String ORGANIZATION_GLOBAL_ID = "12345";
	private static final Dn ORGANIZATION_DN = TestUtil.isimDn(ORGANIZATION_GLOBAL_ID);
	private static final Dn PERSON_DN_1 = TestUtil.uniqueIsimDn("person1");
	private static final Dn PERSON_DN_2 = TestUtil.uniqueIsimDn("person2");
	private static final String ROLE_GLOBAL_ID = "78901";
	private static final String UID = "theUid";
	private static final int MIN_DOMAIN_ADMINS = 2;
	private static final int MAX_DOMAIN_ADMINS = 5;

	@InjectMocks
	private ExternRoleController roleController;

	@Mock
	private RoleService roleService;

	@Mock
	private PersonService personService;
	@Mock
	private AdminDomainService adminDomainService;
	@Mock
	private ApplicationProperties applicationProperties;
	@Mock
	IsimSessionService isimSessionService;
	@Mock
	MonitoringService monitoringService;

	@Mock
	private Model model;

	@Mock
	private RoleObject role;
	@Mock
	private AdminDomainObject adminDomain, updatedAdminDomain;
	@Mock
	private PersonObject person1, person2;
	@Captor
	private ArgumentCaptor<RoleCommand> roleCommandCaptor;

	private IsimUserContextManager isimUserContextManager;

	@BeforeEach
	void setUp() {
		isimUserContextManager = new IsimUserContextManager(isimSessionService, monitoringService);

		lenient().when(applicationProperties.createAdminDomainDn(ORGANIZATION_GLOBAL_ID)).thenReturn(ORGANIZATION_DN);
		lenient().when(applicationProperties.getMaxDomainAdmins()).thenReturn(MAX_DOMAIN_ADMINS);
		lenient().when(applicationProperties.getMinDomainAdmins()).thenReturn(MIN_DOMAIN_ADMINS);

		lenient().when(adminDomain.getDn()).thenReturn(ORGANIZATION_DN);
		lenient().when(person1.getDn()).thenReturn(PERSON_DN_1);
		lenient().when(person2.getDn()).thenReturn(PERSON_DN_2);
	}

	@Test
	void get() {
		when(roleService.findRoleByGlobalId(ROLE_GLOBAL_ID)).thenReturn(Optional.of(role));
		when(adminDomainService.findAdminDomainByDnWithRoles(ORGANIZATION_DN)).thenReturn(Optional.of(adminDomain));

		IsimUserData userData = mock(IsimUserData.class);
		when(userData.getHoofdGebruikersnaam()).thenReturn(UID);
		isimUserContextManager.setContext(userData);

		when(personService.findPersonsFromOrganizationDnWithRoleWithPending(ORGANIZATION_DN, role)).thenReturn(List.of(person1, person2));

		String result = roleController.edit(ORGANIZATION_GLOBAL_ID, ROLE_GLOBAL_ID, model);
		assertThat(result).isEqualTo("/extern/role/rol_detail");

		verify(model).addAttribute(eq("roleCommand"), roleCommandCaptor.capture());
		RoleCommand roleCommand = roleCommandCaptor.getValue();
		assertThat(roleCommand.getRoleObject()).isEqualTo(role);
		assertThat(roleCommand.getPersonObject()).contains(person1, person2);

		verify(model).addAttribute("ingelogde", UID);
		verify(model).addAttribute("administrators_maxcount", MAX_DOMAIN_ADMINS);
		verify(model).addAttribute("administrators_mincount", MIN_DOMAIN_ADMINS);
		verify(model).addAttribute("admindomain", adminDomain);
		verifyNoMoreInteractions(model);
	}

	@Test
	void get_noRole() {
		when(roleService.findRoleByGlobalId(ROLE_GLOBAL_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> roleController.edit(ORGANIZATION_GLOBAL_ID, ROLE_GLOBAL_ID, model))
				.isInstanceOf(RoleNotFoundException.class);
	}

	@Test
	void get_noAdminDomain() {
		when(roleService.findRoleByGlobalId(ROLE_GLOBAL_ID)).thenReturn(Optional.of(role));
		when(adminDomainService.findAdminDomainByDnWithRoles(ORGANIZATION_DN)).thenReturn(Optional.empty());

		String result = roleController.edit(ORGANIZATION_GLOBAL_ID, ROLE_GLOBAL_ID, model);
		assertThat(result).isEqualTo("redirect:/extern/organization/");
	}

	@Test
	void get_noIngelogdePersoon() {
		when(roleService.findRoleByGlobalId(ROLE_GLOBAL_ID)).thenReturn(Optional.of(role));
		when(adminDomainService.findAdminDomainByDnWithRoles(ORGANIZATION_DN)).thenReturn(Optional.of(adminDomain));
		when(personService.findPersonsFromOrganizationDnWithRoleWithPending(ORGANIZATION_DN, role)).thenReturn(List.of(person1, person2));

		String result = roleController.edit(ORGANIZATION_GLOBAL_ID, ROLE_GLOBAL_ID, model);
		assertThat(result).isEqualTo("/extern/role/rol_detail");

		verify(model).addAttribute(eq("roleCommand"), roleCommandCaptor.capture());
		RoleCommand roleCommand = roleCommandCaptor.getValue();
		assertThat(roleCommand.getRoleObject()).isEqualTo(role);
		assertThat(roleCommand.getPersonObject()).contains(person1, person2);

		verify(model).addAttribute("administrators_maxcount", MAX_DOMAIN_ADMINS);
		verify(model).addAttribute("administrators_mincount", MIN_DOMAIN_ADMINS);
		verify(model).addAttribute("admindomain", adminDomain);
	}

	@Test
	@SuppressWarnings("unchecked")
	void post() {
		when(adminDomainService.findAdminDomainByDnWithRoles(ORGANIZATION_DN)).thenReturn(Optional.of(adminDomain), Optional.of(updatedAdminDomain));
		when(person1.isChanged()).thenReturn(true);

		RoleCommand roleCommand = new RoleCommand(role, List.of(person1, person2));
		String result = roleController.doEdit(ORGANIZATION_GLOBAL_ID, ROLE_GLOBAL_ID, roleCommand, model);
		assertThat(result).isEqualTo("redirect:/extern/organization/" + ORGANIZATION_DN.getGlobalId() + "/person/overzicht");

		verify(roleService).changePersonRoleChangeList(List.of(person1, person2), role, adminDomain);
		verify(personService).updatePersonCaches(PERSON_DN_1, adminDomain);

		verify(model).addAttribute("admindomain", Optional.of(updatedAdminDomain));
	}

	@Test
	void post_noAdminDomain() {
		when(adminDomainService.findAdminDomainByDnWithRoles(ORGANIZATION_DN)).thenReturn(Optional.empty());

		RoleCommand roleCommand = new RoleCommand(role, List.of(person1, person2));
		assertThatThrownBy(() -> roleController.doEdit(ORGANIZATION_GLOBAL_ID, ROLE_GLOBAL_ID, roleCommand, model))
				.isInstanceOf(OrganizationNotFoundException.class);
	}
}