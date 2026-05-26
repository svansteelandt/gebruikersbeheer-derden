package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.components.passwordchange.PasswordChangeClient;
import be.vdab.gebruikersbeheer.derden.exception.ITIMTemplateException;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimObjectClasses;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.LdapFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static java.util.Collections.emptyList;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

	private final PasswordChangeClient passwordChangeClient;
	private final IsimLdapManager isimLdapManager;

	@Override
	public boolean tamAccountExistsAndIsDisabledForPerson(String personDn) {
		LdapFilter filter = LdapFilter.andFilter(
				LdapFilter.equalityFilter(IsimAttributeNames.ATTR_PARENT, personDn),
				LdapFilter.objectClassFilter(IsimObjectClasses.ITAM_ACCOUNT),
				LdapFilter.equalityFilter(IsimAttributeNames.ATTR_ACCOUNT_STATUS, "1")
		);
		return !isimLdapManager.getAccountsByFilter(filter, emptyList()).isEmpty();
	}

	@Override
	public void resetPassword(String vdabUid, String emailAdres) {
		try {
			this.passwordChangeClient.sendPasswordForgottenEmail(vdabUid, emailAdres);
		} catch (Exception e) {
			log.error("Sending password forgotten email resulted in exception [{}] with message [{}]", e.getClass(), e.getMessage());
			throw new ITIMTemplateException(e.getClass().getName() + " occurred!", e);
		}
	}
}
