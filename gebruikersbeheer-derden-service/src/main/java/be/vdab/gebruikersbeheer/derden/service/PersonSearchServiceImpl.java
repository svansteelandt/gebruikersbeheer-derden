package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonSearch;
import be.vdab.gebruikersbeheer.derden.domain.PersonsZoekResultaat;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import be.vdab.gebruikersbeheer.util.isim.constants.AdminDomainNames;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonSearchServiceImpl implements PersonSearchService {

	private final PersonService personService;
	private final AdminDomainService adminDomainService;

	@Override
	public PersonsZoekResultaat zoek(@NotNull PersonSearch searchCommand) {

		long start = System.currentTimeMillis();

		var query = FindPersonQuery.builder()
				.gebruikersnaam(searchCommand.getGebruikersnaam())
				.rijksregisternummer(searchCommand.getRijksregisternummer())
				.voornaam(searchCommand.getVoornaam())
				.naam(searchCommand.getNaam())
				.volledigeNaam(searchCommand.getVolledigeNaam())
				.email(searchCommand.getEmail())
				.oe(searchCommand.getOe())
				.limit(searchCommand.getLimit())
				.build();
		var persons = personService.findPersons(query);

		List<Ikp> gevondenIkps = verwijderAuditorsEnBepaalOfAdminDomeinBestaat(persons);

		log.debug("duur zoekactie {} sec.", ((double) System.currentTimeMillis() - start) / 1000);

		return new PersonsZoekResultaat(persons, gevondenIkps);
	}

	private List<Ikp> verwijderAuditorsEnBepaalOfAdminDomeinBestaat(List<PersonObject> persons) {
		var auditors = new ArrayList<PersonObject>();

		var auditorsContainer = adminDomainService.findAdminDomainByName(AdminDomainNames.AUDITORS)
				.orElseThrow(() -> new VestigingNietGevondenException(AdminDomainNames.AUDITORS));
		var auditorsDn = auditorsContainer.getDn();

		List<Ikp> gevondenIkps = new ArrayList<>();
		List<Ikp> opgezochteDIkps = new ArrayList<>();
		Dn parentDn;
		for (var person : persons) {
			parentDn = person.getParentDn();

			if (person.isInactive() && !opgezochteDIkps.contains(person.getIkp())) {
				opgezochteDIkps.add(person.getIkp());
				if (adminDomainService.existsAdminDomain(person.getIkp())) {
					gevondenIkps.add(person.getIkp());
				}
			}

			if (parentDn != null && parentDn.equals(auditorsDn)) {
				auditors.add(person);
			}
		}

		persons.removeAll(auditors);
		return gevondenIkps;
	}
}
