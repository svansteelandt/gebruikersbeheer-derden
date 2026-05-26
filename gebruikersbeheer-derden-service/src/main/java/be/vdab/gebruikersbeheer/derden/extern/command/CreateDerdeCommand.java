package be.vdab.gebruikersbeheer.derden.extern.command;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CreateDerdeCommand {

	private final PersonObject derde;
	@Getter
	private final AdminDomainObject organization;
	@Getter
	private final List<RoleObject> rolesToAdd;

	public String getInsz() {
		return derde.getNationalNumber();
	}

	public String getEmailAddress() {
		return derde.getEmailAddress();
	}

	public String getFullName() {
		return derde.getFullName();
	}

	public PersonObject getPersonToInsert() {
		return derde;
	}

	public Dn getOrganizationDn() {
		return organization.getDn();
	}

	public String getVoornaam() {
		return derde.getFirstName();
	}

	public String getNaam() {
		return derde.getLastName();
	}

	public String getTelefoon() {
		return derde.getPhone();
	}

	public String getGsm() {
		return derde.getMobile();
	}

	public String getIkp() {
		return organization.getIkp().getValue();
	}

	public List<String> getToegangsrechten() {
		return rolesToAdd.stream().map(RoleObject::getRoleName).collect(Collectors.toList());
	}
}
