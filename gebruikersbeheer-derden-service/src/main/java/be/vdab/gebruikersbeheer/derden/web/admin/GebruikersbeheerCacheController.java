package be.vdab.gebruikersbeheer.derden.web.admin;

import be.vdab.gebruikersbeheer.derden.config.CacheNames;
import be.vdab.gebruikersbeheer.derden.domain.ErrorMessage;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.FindPersonQuery;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.util.common.domain.Ikp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.session.hazelcast.HazelcastIndexedSessionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/gebruikersbeheer/v1")
@RequiredArgsConstructor
public class GebruikersbeheerCacheController {

	private final CacheManager cacheManager;
	private final PersonService personService;
	private final AdminDomainService adminDomainService;

	@Operation(summary = "clear alle caches",
			description = "Leeg maken van alle caches",
			responses = {
					@ApiResponse(responseCode = "200", description = "Alle caches zijn leeggemaakt", content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			})
	@GetMapping(value = "cache/clear")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ResponseEntity<ErrorMessage> clearCache() {
		for (String cacheName : this.cacheManager.getCacheNames()) {
			if (!HazelcastIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME.equals(cacheName)) {
				log.debug("Clear cache: {}", cacheName);

				Cache cache = this.cacheManager.getCache(cacheName);
				if (cache != null) {
					cache.clear();
				}
			}
		}

		return new ResponseEntity<>(new ErrorMessage("ALL_CACHES_CLEARED", "Alle caches zijn leeggemaakt"), HttpStatus.OK);
	}

	@Operation(summary = "clear caches voor 1 bepaalde user",
			description = "Leeg maken van de cache voor 1 gebruiker",
			responses = {
					@ApiResponse(responseCode = "200", description = "Alle caches voor username zijn leeggemaakt", content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			})
	@GetMapping(value = "cache/{username}/clear")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ResponseEntity<ErrorMessage> clearCacheForUsername(@PathVariable String username) {
		Cache personCache = this.cacheManager.getCache(CacheNames.CACHE_PERSONS);

		if (personCache != null) {
			var query = FindPersonQuery
					.builder()
					.gebruikersnaam(username)
					.build();
			List<PersonObject> persons = this.personService.findPersons(query);

			if (persons != null) {
				for (PersonObject person : persons) {
					personCache.evict(person.getDn().getGlobalId());
				}
			}
		}

		return new ResponseEntity<>(new ErrorMessage("ALL_CACHES_FOR_USER_CLEARED", "Alle caches zijn voor gebruikersnaam " + username + " leeggemaakt"), HttpStatus.OK);
	}

	@Operation(summary = "clear caches voor 1 bepaald admindomain",
			description = "Leeg maken van de cache voor 1 admin domain",
			responses = {
					@ApiResponse(responseCode = "200", description = "alle caches voor admindomain zijn leeggemaakt", content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
					@ApiResponse(responseCode = "500", description = "Internal server error")
			})
	@GetMapping(value = "cache/clear", params = {"ikp"})
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ResponseEntity<ErrorMessage> clearCacheForOrganisatie(@RequestParam Ikp ikp) {
		Cache adminDomainCache = this.cacheManager.getCache(CacheNames.CACHE_ADMINDOMAIN);

		if (adminDomainCache != null) {
			this.adminDomainService.findAdminDomainByIkp(ikp).ifPresent(adminDomainObject -> {
				adminDomainCache.evict(ikp);
				adminDomainCache.evict(adminDomainObject.getDn());
			});
		}

		return new ResponseEntity<>(new ErrorMessage("ALL_CACHES_FOR_ADMINDOMAIN_CLEARED", "Alle caches zijn voor admindomain " + ikp + " leeggemaakt"), HttpStatus.OK);
	}
}
