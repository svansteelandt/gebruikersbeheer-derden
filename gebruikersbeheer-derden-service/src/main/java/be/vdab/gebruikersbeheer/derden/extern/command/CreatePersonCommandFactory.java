package be.vdab.gebruikersbeheer.derden.extern.command;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import be.vdab.gebruikersbeheer.derden.enumeration.Category;
import be.vdab.gebruikersbeheer.derden.extern.view.PersonCommand;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CreatePersonCommandFactory {

	public CreateDerdeCommand getCreateDerdeCommand(PersonCommand personCommand, AdminDomainObject adminDomainObject) {
		PersonObject derde = new PersonObject(personCommand.getPerson());
		if (!derde.equals(personCommand.getPerson())) {
			throw new RuntimeException();
		}
		derde.setParentDn(adminDomainObject.getDn());
		derde.setUserId("");
		derde.setProfileName(Category.VDABDerde.getType());
		return new CreateDerdeCommand(derde, adminDomainObject, getRolesToAdd(personCommand));
	}

	private List<RoleObject> getRolesToAdd(PersonCommand personCommand) {
		if (personCommand.getHasRoles() != null) {
			return personCommand.getHasRoles().stream()
					.filter(RoleObject::getHasRole)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}
