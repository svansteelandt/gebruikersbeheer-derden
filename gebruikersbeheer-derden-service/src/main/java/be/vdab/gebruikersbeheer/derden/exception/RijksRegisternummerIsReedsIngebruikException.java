package be.vdab.gebruikersbeheer.derden.exception;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import lombok.Getter;

@Getter
public class RijksRegisternummerIsReedsIngebruikException extends ApplicationException {
	private final AdminDomainObject adminDomainObject;

	public RijksRegisternummerIsReedsIngebruikException(AdminDomainObject adminDomainObject) {
		super("Het rijksregisternummer is reeds in gebruik.");
		this.adminDomainObject = adminDomainObject;
	}
}
