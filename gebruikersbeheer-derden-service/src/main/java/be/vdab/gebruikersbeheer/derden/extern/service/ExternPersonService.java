package be.vdab.gebruikersbeheer.derden.extern.service;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.ConfirmNewDerdeCreation;
import be.vdab.gebruikersbeheer.derden.domain.DerdeCreationClaims;
import be.vdab.gebruikersbeheer.derden.domain.DerdeCreationToken;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.TokenExchangeException;
import be.vdab.gebruikersbeheer.derden.extension.FlashMap;
import be.vdab.gebruikersbeheer.derden.extern.command.CreateDerdeCommand;
import be.vdab.gebruikersbeheer.derden.extern.validator.PersonFormValidator;
import be.vdab.gebruikersbeheer.derden.extern.view.PersonCommand;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextHolder;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.security.SecurityExpressions;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.FindPersonQuery;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.derden.service.TokenService;
import be.vdab.gebruikersbeheer.derden.util.RijksregisternummerGenerator;
import be.vdab.gebruikersbeheer.derden.util.isim.support.ContextManager;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternPersonService {

	private final RijksregisternummerGenerator rijksregisternummerGenerator;
	private final PersonFormValidator personFormValidator;
	private final PersonService personService;
	private final RoleService roleService;
	private final TokenService tokenService;
	private final CreateDerdeMailService createDerdeMailService;
	private final AdminDomainService adminDomainService;
	private final SecurityExpressions securityExpressions;
	private final ContextManager contextManager;
	private final IsimUserContextManager isimUserContextManager;

	public boolean validatePersonCommand(PersonCommand personCommand, BindingResult result, Dn organizationDn, Ikp ikp) {
		personFormValidator.validate(personCommand.getPerson(), result);

		if (result.hasErrors()) {
			return false;
		}

		// Is RRN already in this org?
		// Does RRN already exist in de prullenbak for this org?
		boolean rrnExists = personService.rrnExists(personCommand.getPerson().getNationalNumber(), organizationDn);
		Dn existingPersonDn = personService.getPersonDnInPrullenbakForRrnAndIkpNummer(personCommand.getPerson().getNationalNumber(), ikp);

		if (rrnExists) {
			result.rejectValue("person.nationalNumber", "", new Object[]{"Rijksregister"},
					"Het rijksregisternummer is reeds in gebruik.");
		} else if (existingPersonDn != null) {
			FlashMap.setErrorMessage("errorCreate",
					"Je kan deze gebruiker niet zelf aanmaken. Bel hiervoor ons gratis nummer 0800 30 700.");
		}

		return !(rrnExists || existingPersonDn != null);
	}

	public CreatePersonResult createDerdeOrSendMail(CreateDerdeCommand command) {
		var query = FindPersonQuery.builder()
				.rijksregisternummer(command.getInsz())
				.build();
		List<PersonObject> personsWithSameRRN = personService.findPersons(query).stream()
				.filter(PersonObject::isActive)
				.collect(Collectors.toList());

		if (isTokenRequiredForCreation(command, personsWithSameRRN)) {
			DerdeCreationToken derdeCreationToken = tokenService.getDerdeCreationToken(command);
			createDerdeMailService.sendMail(new ConfirmNewDerdeCreation(command, derdeCreationToken, combineRecipients(personsWithSameRRN, command.getEmailAddress())));
			return CreatePersonResult.success("errorCreate", command.getFullName() + " is al gekend als gebruiker en zal een e-mail met instructies ontvangen om dit nieuwe account aan te maken.");
		}

		return createPerson(command);
	}

	CreatePersonResult createPerson(CreateDerdeCommand command) {
		try {
			if (personService.rrnExists(command.getInsz(), command.getOrganizationDn())) {
				return CreatePersonResult.failure("errorCreate", "De nieuwe gebruiker kan niet aangemaakt worden omdat deze al bestaat binnen de opgegeven organisatie.");
			}
			if (personService.getPersonDnInPrullenbakForRrnAndIkpNummer(command.getInsz(), Ikp.of(command.getIkp())) != null) {
				return CreatePersonResult.failure("errorCreate", "Je kan deze gebruiker niet zelf aanmaken. Bel hiervoor ons gratis nummer 0800 30 700.");
			}

			// Als er geen rollen toegekend worden mag de gebruiker aan cache toegevoegd worden, anders gebeurt dit reeds bij toekennen rollen
			final boolean addToCache = command.getRolesToAdd().isEmpty();
			final Dn personDn = personService.insertPerson(command.getOrganization(), command.getPersonToInsert(), addToCache);
			final PersonObject insertedPersonObject = personService.findPersonByDn(personDn, command.getOrganization()).orElseThrow(PersonNotFoundException::new);

			//volgende call enkel uitvoeren indien er rollen zijn toegekend
			if (!command.getRolesToAdd().isEmpty()) {
				insertedPersonObject.setRoles(command.getRolesToAdd());
				roleService.addAndRemoveRoles(insertedPersonObject, command.getOrganization());
				personService.updatePersonCaches(personDn, command.getOrganization());

				waitForRoles(personDn, command.getOrganization());
			}
		} catch (Exception e) {
			log.error("Fout bij het aanmaken van een persoon", e);
			return CreatePersonResult.failure("errorCreate", "Fout bij het aanmaken van een persoon.");
		}

		//enkel bij de eerste account voor een gebruiker (de TAM account dus) moet er gemeld worden dat er een email verstuurd is
		String messageKey = determineMessageKey(command.getPersonToInsert()) ? "insertPerson" : "insertPersonWithoutRoles";

		return CreatePersonResult.success(messageKey, command.getFullName());
	}

	private boolean isTokenRequiredForCreation(CreateDerdeCommand command, List<PersonObject> personsWithSameRRN) {
		List<PersonObject> derdenWithSameEmailAddress = personsWithSameRRN.stream()
				.filter(PersonObject::isActive)
				.filter(person -> command.getEmailAddress().equals(person.getEmailAddress()))
				.collect(Collectors.toList());

		boolean thisIsTheFirstAccount = personsWithSameRRN.isEmpty();
		boolean accountWithSameEmailExists = !derdenWithSameEmailAddress.isEmpty();
		return !(thisIsTheFirstAccount || accountWithSameEmailExists);
	}

	private List<String> combineRecipients(List<PersonObject> derdenWithSameEmailAddress, String newEmailAddress) {
		List<String> result = derdenWithSameEmailAddress.stream().map(PersonObject::getEmailAddress).collect(Collectors.toList());
		result.add(newEmailAddress);
		return result;
	}


	private boolean determineMessageKey(PersonObject personObjectToInsert) {
		return personObjectToInsert.getVdabUid() != null && personObjectToInsert.getVdabUid().equals(personObjectToInsert.getUserId()) && personObjectToInsert.getRoles() != null && !personObjectToInsert.getRoles().isEmpty();
	}

	private void waitForRoles(Dn personDn, AdminDomainObject adminDomainObject) {
		int counter = 0;
		int delay = 10;

		while (counter < delay) {
			PersonObject personObjectNew = personService.findPersonByDn(personDn, adminDomainObject).orElse(null);

			if (personObjectNew != null) {
				if (personObjectNew.getRoles() != null) {
					Set<RoleObject> addedRoles = personObjectNew.getRoles().stream().filter(RoleObject::getHasRole).collect(Collectors.toSet());
					if (!addedRoles.isEmpty()) {
						// rollen zijn toegevoegd
						if (counter > 0) {
							personService.updatePersonCaches(personDn, adminDomainObject);
						}

						break;
					}
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			} else {
				break;
			}

			counter++;
		}
	}

	public CanCreateDerdeResult canCreateDerdeFromToken(String token) {
		try {
			return canCreateDerdeFromTokenForUser(token);
		} catch (TokenExchangeException ex) {
			log.error("Could not exchange token: {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
			return CanCreateDerdeResult.failure(ex.isCausedByBadToken() ? "Deze link is niet (meer) geldig."
					: "Er is een probleem met uw verzoek. Probeer het later opnieuw of contacteer de servicelijn op het nummer 0800 30 700.");
		} catch (Exception ex) {
			log.error("Something bad happened during token exchange: {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
			return CanCreateDerdeResult.failure("Er is een probleem met uw verzoek. Probeer het later opnieuw of contacteer de servicelijn op het nummer 0800 30 700.");
		}
	}

	private CanCreateDerdeResult canCreateDerdeFromTokenForUser(String token) {
		DerdeCreationClaims derdeCreationClaims = tokenService.exchangeTokenForClaims(token);

		if (!IsimUserContextHolder.getContext().getInsz().equals(derdeCreationClaims.getInsz())) {
			return CanCreateDerdeResult.failure("Uw inloggegevens komen niet overeen met het gevraagde profiel.");
		}

		return getOrganisationName(Ikp.of(derdeCreationClaims.getIkp()))
				.map(orgName -> CanCreateDerdeResult.success(derdeData(derdeCreationClaims, orgName)))
				.orElseGet(() -> CanCreateDerdeResult.failure("Er is een probleem met uw verzoek. Probeer het later opnieuw of contacteer uw administrator."));
	}

	private Optional<String> getOrganisationName(Ikp ikp) {
		return adminDomainService.findAdminDomainByIkp(ikp).map(AdminDomainObject::getName);
	}

	private DerdeData derdeData(DerdeCreationClaims derdeCreationClaims, String orgName) {
		return DerdeData.builder()
				.organisationName(orgName)
				.newEmailAdress(derdeCreationClaims.getEmail())
				.newMobileNumber(derdeCreationClaims.getGsm())
				.build();
	}

	public CreatePersonResult createDerdeFromToken(String token) {
		DerdeCreationClaims derdeCreationClaims = tokenService.exchangeTokenForClaims(token);

		PersonObject personObject = new PersonObject();
		personObject.setNationalNumber(derdeCreationClaims.getInsz());
		personObject.setFirstName(derdeCreationClaims.getVoornaam());
		personObject.setLastName(derdeCreationClaims.getNaam());
		personObject.setIkp(Ikp.of(derdeCreationClaims.getIkp()));
		personObject.setEmailAddress(derdeCreationClaims.getEmail());
		personObject.setPhone(derdeCreationClaims.getTelefoon());
		personObject.setMobile(derdeCreationClaims.getGsm());

		Optional<AdminDomainObject> adminDomainObject = adminDomainService.findAdminDomainByIkp(Ikp.of(derdeCreationClaims.getIkp()));
		if (adminDomainObject.isEmpty()) {
			return CreatePersonResult.failure("errorCreate", "Fout bij het aanmaken van een persoon.");
		}

		List<RoleObject> roles = derdeCreationClaims.getToegangsrechten().stream()
				.map(roleService::findByRoleName)
				.map(o -> o.orElse(null))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		roles.forEach(role -> {
			role.setHasRole(true);
			role.setChanged(true);
		});

		try {
			contextManager.startPrivilegedSession();
			return createPerson(new CreateDerdeCommand(personObject, adminDomainObject.get(), roles));
		} finally {
			contextManager.endPrivilegedSession();
		}
	}

	@Data
	public static final class CreatePersonResult {
		private final boolean successful;
		private final String messageKey;
		private final String messageText;

		private CreatePersonResult(boolean successful, String messageKey, String messageText) {
			this.successful = successful;
			this.messageKey = messageKey;
			this.messageText = messageText;
		}

		public static CreatePersonResult failure(String messageKey, String messageText) {
			return new CreatePersonResult(false, messageKey, messageText);
		}

		public static CreatePersonResult success(String messageKey, String messageText) {
			return new CreatePersonResult(true, messageKey, messageText);
		}
	}

	@Data
	public static final class CanCreateDerdeResult {
		final boolean successful;
		final String errorMessage;
		final DerdeData derdeData;

		private CanCreateDerdeResult(boolean successful, String errorMessage, DerdeData derdeData) {
			this.successful = successful;
			this.errorMessage = errorMessage;
			this.derdeData = derdeData;
		}

		public static CanCreateDerdeResult success(DerdeData derdeData) {
			return new CanCreateDerdeResult(true, null, derdeData);
		}

		public static CanCreateDerdeResult failure(String message) {
			return new CanCreateDerdeResult(false, message, null);
		}
	}

	@Data
	@Builder
	public static final class DerdeData {
		String organisationName;
		String newEmailAdress;
		String newMobileNumber;
	}
}
