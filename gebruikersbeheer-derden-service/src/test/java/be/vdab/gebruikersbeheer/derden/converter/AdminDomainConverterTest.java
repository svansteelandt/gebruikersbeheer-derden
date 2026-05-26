package be.vdab.gebruikersbeheer.derden.converter;

import be.vdab.gebruikersbeheer.derden.components.oeservice.OEServiceClient;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.service.PersonOrganizationService;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapAdminDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDomainConverterTest {

	@InjectMocks
	AdminDomainConverter adminDomainConverter;

	@Mock
	IsimLdapAdminDomain adminDomain;

	@Mock
	PersonOrganizationService personOrganizationService;

	@Mock
	RoleService roleService;

	@Mock
	ApplicationProperties applicationProperties;

	@Mock
	OEServiceClient oeServiceClient;

	@BeforeEach
	void setup() {
		when(applicationProperties.getZuil()).thenReturn("ldv");
	}

	@Test
	void convertAdminDomainWithConvert(){
		String oeOmschrijving= init();

		Dn adminDn = new Dn("a");
		when(adminDomain.getAdministrators()).thenReturn(Collections.singleton(adminDn));
		AdminDomainObject adminDomainObject= adminDomainConverter.convert(adminDomain, true);

		validate(adminDomainObject, oeOmschrijving);
	}

	@Test
	void convertAdminDomainWithoutConvert(){
		String oeOmschrijving= init();

		AdminDomainObject adminDomainObject= adminDomainConverter.convert(adminDomain, false);

		validate(adminDomainObject, oeOmschrijving);
	}

	private String init(){
		long oeId= 1881L;
		String oeOmschrijving= "VDAB Centrale Dienst";
		RoleObject adminRole= new RoleObject(Dn.of("erglobalid=456,ou=roles,erglobalid=00000000000000000000,ou=vdab,o=vdab,c=be"));

		when(personOrganizationService.findPersonsByDn(new ArrayList<>())).thenReturn(new ArrayList<>());
		when(adminDomain.getOe()).thenReturn(oeId);
		when(oeServiceClient.getPubliekeOmschrijving(oeId)).thenReturn(oeOmschrijving);
		lenient().when(roleService.findAdminRole()).thenReturn(adminRole);

		return oeOmschrijving;
	}

	private void validate(AdminDomainObject adminDomainObject, String oeOmschrijving){
		assertThat(adminDomainObject).isNotNull();
		assertThat(adminDomainObject.getOeName()).isEqualTo("OE: %s %s".formatted(adminDomain.getOe(), oeOmschrijving));
	}
}
