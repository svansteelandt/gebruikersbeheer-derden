package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.exception.MaximumAantalAdminsOverschredenException;
import be.vdab.gebruikersbeheer.derden.exception.MinimumAantalAdminsNietGehaaldException;
import be.vdab.gebruikersbeheer.derden.exception.RoleNotFoundException;
import be.vdab.gebruikersbeheer.derden.exception.VestigingNietGevondenException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignUsersToRolServiceImpl implements AssignUsersToRolService {
	private final AdminDomainService adminDomainService;
	private final ApplicationProperties applicationProperties;
	private final RoleService roleService;
	private final PersonService personService;

	@Override
	public void assign(@NotNull String globalIdOfOrganisation, @NotNull String globalIdOfRole, @NotNull List<String> personsWhoGetTheRole) throws MinimumAantalAdminsNietGehaaldException, MaximumAantalAdminsOverschredenException {
		var adminDomain = adminDomainService.findAdminDomainByDnWithRoles(applicationProperties.createAdminDomainDn(globalIdOfOrganisation)).orElseThrow(() -> new VestigingNietGevondenException(globalIdOfOrganisation));
		var role = roleService.findRoleByGlobalId(globalIdOfRole).orElseThrow(() -> new RoleNotFoundException(globalIdOfRole));
		var teWijzigenPersonen = personService.findPersonsFromOrganizationDnWithRoleWithPending(adminDomain.getDn(), role);
		valideerAantalAdminsIndienHetEenAdminRolBetreft(personsWhoGetTheRole, role);
		teWijzigenPersonen.forEach(gebruiker -> gebruiker.setHasRole(personsWhoGetTheRole.contains(gebruiker.getDn().getGlobalId())));

		roleService.changePersonRoleChangeList(teWijzigenPersonen, role, adminDomain);

		teWijzigenPersonen.stream().filter(PersonObject::isChanged).forEach(o -> {
			log.trace("person {} {} changed {} hasrole {}", o.getUserId(), o.getDn(), o.isChanged(), o.getHasRole());
			personService.updatePersonCaches(o.getDn(), adminDomain);
		});
	}

	private void valideerAantalAdminsIndienHetEenAdminRolBetreft(List<String> personsWhoGetTheRole, RoleObject role) throws MinimumAantalAdminsNietGehaaldException, MaximumAantalAdminsOverschredenException {
		if (role.isAdminRole()) {
			var aantalAdmins = personsWhoGetTheRole.size();
			if (aantalAdmins < applicationProperties.getMinDomainAdmins()) {
				throw new MinimumAantalAdminsNietGehaaldException(applicationProperties.getMinDomainAdmins());
			} else if (aantalAdmins > applicationProperties.getMaxDomainAdmins()) {
				throw new MaximumAantalAdminsOverschredenException(applicationProperties.getMaxDomainAdmins());
			}
		}
	}
}
