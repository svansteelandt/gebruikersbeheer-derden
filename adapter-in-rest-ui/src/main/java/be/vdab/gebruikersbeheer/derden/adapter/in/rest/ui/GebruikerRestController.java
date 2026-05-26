package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper.GebruikerMapper;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerSummaryDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.GebruikerZoekResultaatDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.ChangeLoginMethodCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.CreateGebruikerCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.EditGebruikerCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.commands.WijsRollenToeAanGebruikerCommand;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.search.LimitedSearch;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.search.LimitedSearchResult;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonSearch;
import be.vdab.gebruikersbeheer.derden.domain.PersonsZoekResultaat;
import be.vdab.gebruikersbeheer.derden.domain.RoleAssignmentResult;
import be.vdab.gebruikersbeheer.derden.exception.GebruikerBestaatReedsOpDezeVestigingException;
import be.vdab.gebruikersbeheer.derden.exception.GebruikerCreateFoutException;
import be.vdab.gebruikersbeheer.derden.exception.GebruikerHeeftGeenEmailAdresException;
import be.vdab.gebruikersbeheer.derden.exception.LoginMethodeCouldNotBeChangedException;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.RijksRegisternummerIsReedsIngebruikException;
import be.vdab.gebruikersbeheer.derden.exception.RoleNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.SearchCriteriaVerplichtException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import be.vdab.gebruikersbeheer.derden.service.AccountService;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.MinimumAdminsReachedException;
import be.vdab.gebruikersbeheer.derden.service.PersonChangeRolesService;
import be.vdab.gebruikersbeheer.derden.service.PersonCreateService;
import be.vdab.gebruikersbeheer.derden.service.PersonRestoreService;
import be.vdab.gebruikersbeheer.derden.service.PersonSearchService;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/ui")
@Slf4j
@RequiredArgsConstructor
public class GebruikerRestController {
	private final PersonService personService;
	private final AdminDomainService adminDomainService;
	private final ApplicationProperties applicationProperties;
	private final GebruikerMapper gebruikerMapper;
	private final PersonCreateService personCreateService;
	private final PersonSearchService personSearchService;
	private final PersonRestoreService personRestoreService;
	private final AccountService accountService;
	private final PersonChangeRolesService personChangeRolesService;
	private final RoleService roleService;

	@GetMapping("gebruikers")
	public GebruikerZoekResultaatDto zoek(@RequestParam(required = false) @Size(max = 20) String gebruikersNaam,
	                                      @RequestParam(required = false) @Size(max = 12) String rijksregisterNummer,
	                                      @RequestParam(required = false) @Size(max = 50) String voornaam,
	                                      @RequestParam(required = false) @Size(max = 50) String naam,
	                                      @RequestParam(required = false) @Size(max = 100) String volledigeNaam,
	                                      @RequestParam(required = false) @Size(max = 320) String email,
	                                      @RequestParam(required = false) Long oeNummer) throws SearchCriteriaVerplichtException {
		var searchCommand = new PersonSearch();
		searchCommand.setGebruikersnaam(gebruikersNaam);
		searchCommand.setNaam(naam);
		searchCommand.setVoornaam(voornaam);
		searchCommand.setVolledigeNaam(volledigeNaam);
		searchCommand.setRijksregisternummer(rijksregisterNummer);
		searchCommand.setEmail(email);
		if (oeNummer != null) {
			searchCommand.setOe(Long.toString(oeNummer));
		}

		if (!searchCommand.hasSearchCriteria()) {
			throw new SearchCriteriaVerplichtException();
		}

		LimitedSearchResult<PersonsZoekResultaat, PersonObject> zoekResultaat = new LimitedSearch<>(searchCommand, PersonSearch::setLimit)
				.setLimit(applicationProperties.getUiSearchLimit())
				.execute(personSearchService::zoek, PersonsZoekResultaat::persons);

		List<String> ikpDisplayValues = zoekResultaat.result()
				.gevondenIkps()
				.stream()
				.map(Ikp::getDisplayValue)
				.toList();
		return new GebruikerZoekResultaatDto(gebruikerMapper.map(zoekResultaat.limitedList()), ikpDisplayValues, zoekResultaat.tooManyResults());

	}

	@GetMapping("gebruikers/{globalId}")
	public GebruikerDto getGebruiker(@PathVariable String globalId) throws PersonNotFoundException {
		var person = personService.findPersonWithRolesByGlobalId(globalId).orElseThrow(() -> new PersonNotFoundException(globalId));

		return gebruikerMapper.toGebruiker(person);
	}

	@PutMapping("gebruikers/{globalId}/login-methode")
	@PreAuthorize("@SecurityExpressions.hasRole('" + RoleNames.ROL_CVS_RFI + "')")
	public void changeLoginMethod(@PathVariable("globalId") String personId,
	                              @RequestBody() ChangeLoginMethodCommand loginMethodCommand) throws LoginMethodeCouldNotBeChangedException {
		var resultChange = personService.changeLoginMethod(personId, loginMethodCommand.loginMethod().getValue());

		if (!resultChange) {
			throw new LoginMethodeCouldNotBeChangedException();
		}
	}

	@PutMapping("gebruikers/{globalId}/rollen")
	@PreAuthorize("@SecurityExpressions.isBeheerderDerden()")
	public RoleAssignmentResult wijzigRollenGebruiker(@PathVariable("globalId") String personId,
	                                                  @RequestBody() WijsRollenToeAanGebruikerCommand command) {
		return personChangeRolesService.changeRoles(personId, command.globalIdsVanRollen(), command.cvsRole());
	}

	@DeleteMapping("gebruikers/{globalId}/wachtwoord")
	public void verstuurResetWachtwoordMail(@PathVariable("globalId") String personId) throws GebruikerHeeftGeenEmailAdresException {
		var personDn = this.applicationProperties.createPersonDn(personId);
		var personObject = personService.findPersonByDn(personDn, null).orElseThrow(() -> new PersonNotFoundException(personId));

		if (StringUtils.isNotBlank(personObject.getEmailAddress())) {
			accountService.resetPassword(personObject.getUserId(), personObject.getEmailAddress().trim());
		} else {
			throw new GebruikerHeeftGeenEmailAdresException(personId);
		}
	}

	@PutMapping("gebruikers/{globalId}/status")
	@PreAuthorize("@SecurityExpressions.isBeheerderDerden()")
	public boolean restore(@PathVariable("globalId") String personGlobalId) {
		return personRestoreService.restore(personGlobalId);
	}

	@DeleteMapping("vestigingen/{vestigingGlobalId}/gebruikers/{gebruikerGlobalId}")
	@PreAuthorize("@SecurityExpressions.isBeheerderDerden()")
	public void deleteGebruiker(@PathVariable String vestigingGlobalId, @PathVariable String gebruikerGlobalId) throws PersonNotFoundException, MinimumAdminsReachedException {
		var adminDomainObject = this.adminDomainService.findAdminDomainByDnWithRoles(this.applicationProperties.createAdminDomainDn(vestigingGlobalId)).orElseThrow(() -> new VestigingNietGevondenException(vestigingGlobalId));
		var personObject = this.personService.findPersonByDn(this.applicationProperties.createPersonDn(gebruikerGlobalId), adminDomainObject).orElseThrow(() -> new PersonNotFoundException("Gebruiker %s werd niet gevonden.".formatted(gebruikerGlobalId)));

		personService.deletePerson(adminDomainObject, personObject);
	}

	@GetMapping("vestigingen/{globalId}/gebruikers")
	@PreAuthorize("@SecurityExpressions.isIntern()")
	public List<GebruikerSummaryDto> getGebruikersVanVestiging(@PathVariable String globalId) {
		var organizationDn = applicationProperties.createAdminDomainDn(globalId);
		var adminDomainObject = adminDomainService.findAdminDomainByDnWithRoles(organizationDn)
				.orElseThrow(() -> new VestigingNietGevondenException(globalId));

		var persons = personService.findNonMlpOpleidingPersonsFromOrganization(adminDomainObject);
		return gebruikerMapper.map(persons);
	}

	@GetMapping("vestigingen/{globalId}/gebruikers/verwijderd")
	@PreAuthorize("@SecurityExpressions.isIntern()")
	public List<GebruikerSummaryDto> getVerwijderdeGebruikersVanVestiging(@PathVariable String globalId) {
		var organizationDn = applicationProperties.createAdminDomainDn(globalId);
		var adminDomainObject = adminDomainService.findAdminDomainByDnWithRoles(organizationDn)
				.orElseThrow(() -> new VestigingNietGevondenException(globalId));

		var persons = personService.findPersonsInPrullenbakForIkpNummer(adminDomainObject.getIkp());
		return gebruikerMapper.map(persons);
	}

	@PostMapping("vestigingen/{globalId}/gebruikers")
	@PreAuthorize("@SecurityExpressions.isBeheerderDerden()")
	public GebruikerDto createGebruiker(@PathVariable String globalId, @Valid @RequestBody CreateGebruikerCommand createGebruikerCommand) throws BindException, VestigingNietGevondenException, RijksRegisternummerIsReedsIngebruikException, GebruikerBestaatReedsOpDezeVestigingException, GebruikerCreateFoutException {
		var personToCreate = gebruikerMapper.mapToPerson(createGebruikerCommand);
		var roles = createGebruikerCommand.roleGlobalIds()
				.stream()
				.map(id -> roleService.findRoleByGlobalId(id)
						.map(role -> {
							role.setChanged(true);
							role.setHasRole(true);
							return role;
						})
						.orElseThrow(() -> new RoleNotFoundException(id))
				)
				.toList();

		return gebruikerMapper.toGebruiker(this.personCreateService.create(globalId, personToCreate, roles));
	}

	@PutMapping("gebruikers/{gebruikerGlobalId}")
	@PreAuthorize("@SecurityExpressions.isBeheerderDerden()")
	public GebruikerDto editGebruiker(@PathVariable String gebruikerGlobalId, @Valid @RequestBody EditGebruikerCommand editCommand) {
		PersonObject person = personService.findPersonWithRolesByGlobalId(gebruikerGlobalId).orElseThrow(() -> new PersonNotFoundException(gebruikerGlobalId));
		person.setFirstName(editCommand.firstName());
		person.setLastName(editCommand.lastName());
		person.setEmailAddress(editCommand.email());
		person.setPhone(editCommand.phone());
		person.setMobile(editCommand.mobile());
		person.setSuspend(editCommand.suspend());
		person.setSuspendOmschrijving(editCommand.suspendOmschrijving());
		personService.updatePerson(person);

		return gebruikerMapper.toGebruiker(person);
	}
}
