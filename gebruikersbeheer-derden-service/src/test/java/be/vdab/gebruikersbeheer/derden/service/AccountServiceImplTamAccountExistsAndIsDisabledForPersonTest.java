package be.vdab.gebruikersbeheer.derden.service;


import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimObjectClasses;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapAccount;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.LdapFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTamAccountExistsAndIsDisabledForPersonTest {

	private static final String PERSON_DN = "thePersonDn";
	private static final LdapFilter EXPECTED_FILTER = LdapFilter.andFilter(
		LdapFilter.equalityFilter(IsimAttributeNames.ATTR_PARENT, PERSON_DN),
		LdapFilter.objectClassFilter(IsimObjectClasses.ITAM_ACCOUNT),
		LdapFilter.equalityFilter(IsimAttributeNames.ATTR_ACCOUNT_STATUS, "1")
	);

	@InjectMocks
	private AccountServiceImpl accountService;

	@Mock
	private IsimLdapManager isimLdapManager;

	@Mock
	private IsimLdapAccount account;

	@Test
	void returnsTrueWhenTamAccountExistsAndIsDisabled() {
		when(isimLdapManager.getAccountsByFilter(EXPECTED_FILTER, emptyList())).thenReturn(singletonList(account));
		assertTrue(accountService.tamAccountExistsAndIsDisabledForPerson(PERSON_DN));
	}

	@Test
	void returnsFalseWhenTamAccountExistsAndIsDisabled() {
		when(isimLdapManager.getAccountsByFilter(EXPECTED_FILTER, emptyList())).thenReturn(emptyList());
		assertFalse(accountService.tamAccountExistsAndIsDisabledForPerson(PERSON_DN));
	}
}