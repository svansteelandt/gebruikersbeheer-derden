package be.vdab.gebruikersbeheer.derden.web.internal.v1;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.ErrorMessage;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.PersonNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.RoleNotFoundException;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.MinimumAdminsReachedException;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.util.common.constants.RoleNames;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/internal/gebruikersbeheer/v1")
public class PersonenController {

	private final AdminDomainService adminDomainService;

	private final PersonService personService;

	private final ApplicationProperties applicationProperties;

	public PersonenController(AdminDomainService adminDomainService, PersonService personService, ApplicationProperties applicationProperties) {
		this.adminDomainService = adminDomainService;
		this.personService = personService;
		this.applicationProperties = applicationProperties;
	}

	@Operation(summary = "Toevoegen van een rol aan een gebruiker",
			description = "Toevoegen van een rol aan een gebruiker",
			responses = {
					@ApiResponse(responseCode = "200", description = "Toevoegen van een rol aan een persoon", content = @Content(schema = @Schema(implementation = PersonObject.class))),
					@ApiResponse(responseCode = "400", description = "Het toevoegen van rol kan niet afgwerkt worden met de opgegeven data : rol niet gevonden (ROLE_NOT_FOUND).", content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
					@ApiResponse(responseCode = "404", description = "Gebruiker werd niet gevonden (PERSON_NOT_FOUND)", content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
					@ApiResponse(responseCode = "422", description = "Request timeout"),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			})
	@PostMapping(value = {"gebruikers/{gebruikersNaam}/rollen/{rol}", "personen/{gebruikersNaam}/rollen/{rol}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> toevoegenRol(@PathVariable String gebruikersNaam, @PathVariable String rol) {
		if (StringUtils.isBlank(rol)) {
			return new ResponseEntity<>("Rol required.", HttpStatus.NOT_ACCEPTABLE);
		} else {
			try {
				this.personService.toevoegenRol(gebruikersNaam, rol);

				return new ResponseEntity<>(this.personService.findPersonByGebruikersnaam(gebruikersNaam), HttpStatus.OK);
			} catch (PersonNotFoundException p) {
				return new ResponseEntity<>(new ErrorMessage("PERSON_NOT_FOUND", "Gebruiker '" + gebruikersNaam + "' is niet gevonden."), HttpStatus.NOT_FOUND);
			} catch (RoleNotFoundException r) {
				return new ResponseEntity<>(new ErrorMessage("ROLE_NOT_FOUND", "Rol '" + rol + "' is niet gevonden."), HttpStatus.BAD_REQUEST);
			}
		}
	}

	@Operation(summary = "Verwijderen van een rol van een gebruiker",
			description = "Verwijderen van een rol van een gebruiker",
			responses = {
					@ApiResponse(responseCode = "200", description = "Verwijderen van een rol aan een persoon", content = @Content(schema = @Schema(implementation = PersonObject.class))),
					@ApiResponse(responseCode = "400", description = "Het verwijderen van rol kan niet afgwerkt worden met de opgegeven data : rol niet gevonden (ROLE_NOT_FOUND).", content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
					@ApiResponse(responseCode = "404", description = "Gebruiker werd niet gevonden (PERSON_NOT_FOUND)", content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
					@ApiResponse(responseCode = "422", description = "Request timeout"),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			})
	@DeleteMapping(value = {"gebruikers/{gebruikersNaam}/rollen/{rol}", "personen/{gebruikersNaam}/rollen/{rol}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> verwijderenApplicatieveRol(@PathVariable String gebruikersNaam, @PathVariable String rol) {
		if (StringUtils.isBlank(rol)) {
			return new ResponseEntity<>("Rol required.", HttpStatus.NOT_ACCEPTABLE);
		} else {
			try {
				this.personService.verwijderenRol(gebruikersNaam, rol);

				return new ResponseEntity<>(personService.findPersonByGebruikersnaam(gebruikersNaam), HttpStatus.OK);
			} catch (PersonNotFoundException p) {
				return new ResponseEntity<>(new ErrorMessage("PERSON_NOT_FOUND", "Gebruiker '" + gebruikersNaam + "' is niet gevonden."), HttpStatus.NOT_FOUND);
			} catch (RoleNotFoundException r) {
				return new ResponseEntity<>(new ErrorMessage("ROLE_NOT_FOUND", "Rol '" + rol + "' is niet gevonden."), HttpStatus.BAD_REQUEST);
			}
		}
	}

	@Operation(summary = "Verwijderen van een gebruiker",
			description = "Verwijderen van een gebruiker",
			responses = {
					@ApiResponse(responseCode = "200", description = "Verwijderen van een rol aan een persoon"),
					@ApiResponse(responseCode = "404", description = "Gebruiker werd niet gevonden (PERSON_NOT_FOUND)", content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			})
	@PreAuthorize("@SecurityExpressions.hasRole('" + RoleNames.ROL_BEHEERDERS_DERDEN + "')|| @SecurityExpressions.hasRole('" + RoleNames.ROL_BEHEERDERS_DERDEN_ZONDER_RRN + "')")
	@DeleteMapping(value = "personen/{personId}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> verwijderenGebruiker(@PathVariable(value = "personId") String globalId) {
		if (StringUtils.isEmpty(globalId)) {
			return ResponseEntity.badRequest().body("Ontbrekende personId");
		}
		Optional<PersonObject> optionalPersonObject = fetchPerson(globalId);

		if (optionalPersonObject.isPresent()) {
			PersonObject personObject = optionalPersonObject.get();

			Optional<AdminDomainObject> optionalAdminDomainObject = adminDomainService.findAdminDomainByDn(personObject.getParentDn());

			if (optionalAdminDomainObject.isPresent()) {
				try {
					if (this.personService.deletePerson(optionalAdminDomainObject.get(), personObject)) {

						return ResponseEntity.ok().build();
					} else {
						return new ResponseEntity<>(new ErrorMessage("REMOVE_FAILED", "Verwijderen gebruiker is gefaald"), HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} catch (MinimumAdminsReachedException minimumAdminsReachedException) {
					return ResponseEntity.badRequest().body("Laatste admin mag niet verwijderd worden");
				}
			} else {
				return ResponseEntity.notFound().build();
			}
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Operation(summary = "Geeft lijst van rollen voor deze persoon",
			description = "Geeft lijst van rollen voor deze persoon",
			responses = {
					@ApiResponse(responseCode = "200", description = "Geeft lijst van rollen terug", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
					@ApiResponse(responseCode = "404", description = "Persoon of zijn organisatie bestaat niet"),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			})
	@GetMapping("personen/{globalId}/rollen")
	@PreAuthorize("@SecurityExpressions.isBeheerderDerden()")
	public List<String> getRollenForPersoon(@PathVariable String globalId) {
		PersonObject person = fetchPerson(globalId).orElseThrow(PersonNotFoundException::new);
		if (person == null) throw new PersonNotFoundException(globalId);

		return person.getRoles().stream()
				.filter(RoleObject::getHasRole)
				.map(RoleObject::getRoleName)
				.collect(Collectors.toList());
	}

	private Optional<PersonObject> fetchPerson(String globalId) {
		Dn personDn =applicationProperties.createPersonDn(globalId);
		return this.personService.findPersonByDn(personDn, null);
	}
}
