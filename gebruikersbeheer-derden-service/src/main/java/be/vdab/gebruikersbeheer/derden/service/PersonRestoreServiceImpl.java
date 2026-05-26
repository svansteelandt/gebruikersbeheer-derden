package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonRestoreServiceImpl implements PersonRestoreService {
	private final PersonService personService;
	private final ApplicationProperties applicationProperties;
	private final AdminDomainService adminDomainService;
	private final AccountService accountService;

	@Override
	public boolean restore(String personGlobalId) throws PersonNotFoundException, VestigingNietGevondenException {
		var personObject = this.personService.findPersonByDn(this.applicationProperties.createPersonDn(personGlobalId), null)
				.orElseThrow(() -> new PersonNotFoundException(personGlobalId));
		if (personObject.getIkp() != null) {
			this.adminDomainService.findAdminDomainByIkp(personObject.getIkp()).orElseThrow(() -> new VestigingNietGevondenException(personObject.getIkp().getDisplayValue()));
		}

		if (personService.restorePerson(personObject)) {
			// after restore, send email for new password
			if (StringUtils.isNotBlank(personObject.getEmailAddress())) {
				accountService.resetPassword(personObject.getUserId(), personObject.getEmailAddress().trim());
			}
			return true;
		} else {
			return false;
		}
	}
}
