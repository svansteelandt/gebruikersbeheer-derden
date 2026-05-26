package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.converter.PersonConverter;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapManager;
import be.vdab.gebruikersbeheer.util.isim.ldap.IsimLdapPerson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonOrganisationServiceImpl implements PersonOrganizationService {
	private final IsimLdapManager isimLdapManager;
	private final PersonConverter personConverter;

	@Override
	public boolean existsPersonInOrganization(Dn personDn, Dn adminDomainDn) {
		return isimLdapManager.getParentDn(personDn).filter(parentDn -> parentDn.equals(adminDomainDn)).isPresent();
	}

	@NonNull
	@Override
	public List<PersonObject> findPersonsByDn(List<Dn> dns) {
		log.debug("findPersonsByDn(List dns): {}", dns);

		long start = System.currentTimeMillis();

		List<IsimLdapPerson> persons = new ArrayList<>();
		for (Dn personDn : dns) {
			isimLdapManager.getPersonByDn(personDn, IsimAttributeNames.DEFAULT_PERSON_ATTRIBUTES_DERDEN).ifPresent(persons::add);
		}

		List<PersonObject> personObjects = personConverter.convertList(persons, false);

		if (log.isTraceEnabled()) {
			log.trace("duur findPersonsByDn(List dns): {} sec", ((double) System.currentTimeMillis() - start) / 1000);
		}

		return personObjects;
	}
}
