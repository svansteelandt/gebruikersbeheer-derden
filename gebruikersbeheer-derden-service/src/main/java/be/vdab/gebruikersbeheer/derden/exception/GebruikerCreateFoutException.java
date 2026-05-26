package be.vdab.gebruikersbeheer.derden.exception;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import lombok.Getter;

@Getter
public class GebruikerCreateFoutException extends ApplicationException {
	private final AdminDomainObject adminDomainObject;

	public GebruikerCreateFoutException(AdminDomainObject adminDomainObject) {
		super("Fout bij het creëren van een persoon.");
		this.adminDomainObject = adminDomainObject;
	}
}
