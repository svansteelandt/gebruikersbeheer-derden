package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingSummaryDto;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class VestigingMapperTest {
	static final String VESTIGING_NAAM = "naam";

	static final String OE_NAAM= "OE 123";
	static final String GEMEENTE = "g";
	static final String STRAAT = "str";
	static final String POSTCODE = "2900";
	static final String KBO_NUMMER = "k";
	static final String GLOBAL_ID = "G1";
	static final String ADMINISTRATOR_GLOBAL_ID = "AG1";
	static final String VIRTUAL_ADMINISTRATOR_GLOBAL_ID = "VAG1";

	@InjectMocks
	VestigingMapper vestigingMapper;

	@Test
	@DisplayName("""
                   GIVEN valid list of AdminDomainObject
                   WHEN map is called
                   THEN valid list of VestigingSummaryDto is returned
                   """)
	void mapAdminDomainObjects() {
		var adminDomainObject = new AdminDomainObject();
		adminDomainObject.setGlobalId(GLOBAL_ID);
		adminDomainObject.setName(VESTIGING_NAAM);
		adminDomainObject.setPostalcode(POSTCODE);
		adminDomainObject.setCity(GEMEENTE);
		adminDomainObject.setStreet(STRAAT);
		adminDomainObject.setIkp(Ikp.of(203000L));
		adminDomainObject.setKboNummer(KBO_NUMMER);

		var vestiging = new VestigingSummaryDto(GLOBAL_ID, VESTIGING_NAAM, GEMEENTE, POSTCODE, STRAAT, "203-000", KBO_NUMMER);
		Assertions.assertThat(vestigingMapper.map(List.of(adminDomainObject))).usingRecursiveComparison().isEqualTo(List.of(vestiging));
	}

	@Test
	@DisplayName("""
			GIVEN valid list of AdminDomainObject
			WHEN map is called
			THEN valid VestigingDto is returned
			""")
	void mapAdminDomainObject() {
		var admininstrator = new PersonObject();
		admininstrator.setDn(Dn.of("erglobalid=" + ADMINISTRATOR_GLOBAL_ID));

		mapAdminDomainObject(List.of(admininstrator));
	}

	@Test
	@DisplayName("""
			GIVEN valid list of AdminDomainObject with administrators
			WHEN map is called
			THEN valid VestigingDto is returned with only named administrators (no virtual admins)
			""")
	void mapAdminDomainObjectIgnoreVirtualAdmin(){
		var namedAdmininstrator = new PersonObject();
		namedAdmininstrator.setDn(Dn.of("erglobalid=" + ADMINISTRATOR_GLOBAL_ID));

		var virtualAdmininstrator = new PersonObject();
		virtualAdmininstrator.setDn(Dn.of("erglobalid=" + VIRTUAL_ADMINISTRATOR_GLOBAL_ID));
		virtualAdmininstrator.setProfileName("vdabvirtual");
		virtualAdmininstrator.setVirtualAccount(true);

		List<PersonObject> admininstrators= new ArrayList<>();
		admininstrators.add(namedAdmininstrator);
		admininstrators.add(virtualAdmininstrator);

		mapAdminDomainObject(admininstrators);
	}

	private void mapAdminDomainObject(List<PersonObject> admininstrators){
		var adminDomainObject = new AdminDomainObject();
		adminDomainObject.setDn(Dn.of("erglobalid=" + GLOBAL_ID));
		adminDomainObject.setName(VESTIGING_NAAM);
		adminDomainObject.setOeName(OE_NAAM);
		adminDomainObject.setGlobalId(GLOBAL_ID);
		adminDomainObject.setSamakks(Set.of("S1"));
		adminDomainObject.setRoles(List.of(new RoleObject()));
		adminDomainObject.setAdministrators(admininstrators);

		var vestiging= new VestigingDto(adminDomainObject.getDn().getGlobalId(),
				VESTIGING_NAAM,
				OE_NAAM,
				adminDomainObject.getSamakks(),
				adminDomainObject.getRoles(),
				adminDomainObject.getAdministrators().stream().filter(p -> !p.isVirtualAccount()).map(a -> a.getDn().getGlobalId()).toList());

		Assertions.assertThat(vestigingMapper.map(adminDomainObject)).usingRecursiveComparison().isEqualTo(vestiging);
	}
}
