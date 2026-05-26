package be.vdab.gebruikersbeheer.derden.exception;

public class MaximumAantalAdminsOverschredenException extends ApplicationException {
	public MaximumAantalAdminsOverschredenException(int maxDomainAdmins) {
		super("Maximum aantal admins (%s) overschreven.".formatted(maxDomainAdmins));
	}
}
