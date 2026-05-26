package be.vdab.gebruikersbeheer.derden.service;


import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimObjectClasses;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.LdapFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplGetPersonDnInPrullenbakForRrnAndIkpNummerTest {

	private static final Dn PRULLENBAK_DN = Dn.of("erglobalid=3039778591881921860,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,O=VDAB,C=BE");
	private static final Dn PERSON_DN = Dn.of("erglobalid=6557533043368281674,ou=0,ou=people,erglobalid=00000000000000000000,ou=vdab,O=VDAB,C=BE");

	private static final String RRN = "21020106074";
	private static final Ikp IKP = Ikp.of("145592000");

	private static final LdapFilter EXPECTED_QUERY = LdapFilter.andFilter(
			LdapFilter.objectClassFilter(IsimObjectClasses.VDAB_DERDE),
			LdapFilter.equalityFilter(IsimAttributeNames.ATTR_IKP, IKP),
			LdapFilter.equalityFilter(IsimAttributeNames.ATTR_RIJKSREGISTERNUMMER, RRN),
			LdapFilter.equalityFilter(IsimAttributeNames.ATTR_PARENT, PRULLENBAK_DN)
	);

	@InjectMocks
	private PersonServiceImpl personService;

	@Mock
	private IsimLdapManager isimLdapManager;

	@BeforeEach
	void initializePersonDao() {
		lenient().when(isimLdapManager.getPrullenbakDn()).thenReturn(PRULLENBAK_DN);
	}

	@Test
	void returnPersonDnWhenTheQueryReturnsResults() {
		when(isimLdapManager.getPersonsDnsByFilter(EXPECTED_QUERY)).thenReturn(singletonList(PERSON_DN));
		assertThat(personService.getPersonDnInPrullenbakForRrnAndIkpNummer(RRN, IKP)).isEqualTo(PERSON_DN);
	}

	@Test
	void returnsNullWhenQueryReturnsEmptyList() {
		when(isimLdapManager.getPersonsDnsByFilter(EXPECTED_QUERY)).thenReturn(emptyList());
		assertThat(personService.getPersonDnInPrullenbakForRrnAndIkpNummer(RRN, IKP)).isNull();
	}

	@Test
	void returnsNullWhenQueryReturnsNull() {
		when(isimLdapManager.getPersonsDnsByFilter(EXPECTED_QUERY)).thenReturn(emptyList());
		assertThat(personService.getPersonDnInPrullenbakForRrnAndIkpNummer(RRN, IKP)).isNull();
	}
}