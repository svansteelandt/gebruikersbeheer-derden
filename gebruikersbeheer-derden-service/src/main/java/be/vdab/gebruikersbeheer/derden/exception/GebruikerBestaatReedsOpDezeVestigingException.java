package be.vdab.gebruikersbeheer.derden.exception;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import lombok.Getter;

@Getter
public class GebruikerBestaatReedsOpDezeVestigingException extends ApplicationException {
	private final AdminDomainObject adminDomainObject;

	public GebruikerBestaatReedsOpDezeVestigingException(AdminDomainObject adminDomainObject) {
		super("De gebruiker bestaat reeds op deze vestiging.");
		this.adminDomainObject = adminDomainObject;
	}
}
