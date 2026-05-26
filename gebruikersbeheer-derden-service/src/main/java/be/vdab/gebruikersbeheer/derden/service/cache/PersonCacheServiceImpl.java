package be.vdab.gebruikersbeheer.derden.service.cache;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.service.AdminService;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@RequiredArgsConstructor
public class PersonCacheServiceImpl implements PersonCacheService {

	private final ApplicationProperties applicationProperties;
	private final CacheService cacheService;
	private final PersonService personService;
	private final AdminService adminService;

	@Override
	public void clearVestigingCache(@PathVariable() String globalId) {
		var organizationDn = applicationProperties.createAdminDomainDn(globalId);
		adminService.deleteFromAdminDomainCache(organizationDn);

		var personDns = this.personService.findPersonsFromOrganization(organizationDn);

		if (personDns != null) {
			for (var personDn : personDns) {
				this.cacheService.deleteFromPersonCaches(personDn);
			}
		}
	}
}
