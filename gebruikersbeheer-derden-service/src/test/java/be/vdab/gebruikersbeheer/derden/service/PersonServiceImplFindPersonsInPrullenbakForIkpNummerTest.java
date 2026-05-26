package be.vdab.gebruikersbeheer.derden.service;


import be.vdab.gebruikersbeheer.derden.converter.PersonConverter;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimObjectClasses;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.LdapFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplFindPersonsInPrullenbakForIkpNummerTest {

	private static final Dn PRULLENBAK_DN = Dn.of("erglobalid=3039778591881921860,ou=orgChart,erglobalid=00000000000000000000,ou=vdab,O=VDAB,C=BE");
	private static final Ikp IKP = Ikp.of("145592000");

	private static final LdapFilter EXPECTED_QUERY = LdapFilter.andFilter(
			LdapFilter.objectClassFilter(IsimObjectClasses.VDAB_DERDE),
			LdapFilter.equalityFilter(IsimAttributeNames.ATTR_IKP, IKP),
			LdapFilter.equalityFilter(IsimAttributeNames.ATTR_PARENT, PRULLENBAK_DN)
	);

	@InjectMocks
	private PersonServiceImpl personService;

	@Mock
	private IsimLdapManager isimLdapManager;

	@Mock
	private PersonConverter personConverter;

	@Mock
	private IsimLdapPerson person1, person2;
	@Mock
	private PersonObject personObject1, personObject2;

	@BeforeEach
	void setUp() {
		lenient().when(isimLdapManager.getPrullenbakDn()).thenReturn(PRULLENBAK_DN);
	}

	@Test
	void returnsThePersonObjects() {
		List<IsimLdapPerson> persons = List.of(person1, person2);
		List<PersonObject> personObjects = List.of(personObject1, personObject2);
		when(isimLdapManager.getPersonsByFilter(EXPECTED_QUERY, IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN))
				.thenReturn(persons);
		when(personConverter.convertList(persons, false)).thenReturn(personObjects);

		assertThat(personService.findPersonsInPrullenbakForIkpNummer(IKP)).isEqualTo(personObjects);
	}
}