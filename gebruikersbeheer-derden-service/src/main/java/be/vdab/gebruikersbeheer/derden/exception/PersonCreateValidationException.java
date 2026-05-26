package be.vdab.gebruikersbeheer.derden.exception;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.intern.command.PersonCommand;
import lombok.Getter;
import org.springframework.validation.BindException;

@Getter
public class PersonCreateValidationException extends BindException {
	private final AdminDomainObject adminDomainObject;

	public PersonCreateValidationException(PersonCommand command, AdminDomainObject adminDomainObject) {
		super(command, "createGebruikerCommand");
		this.adminDomainObject = adminDomainObject;
	}
}
