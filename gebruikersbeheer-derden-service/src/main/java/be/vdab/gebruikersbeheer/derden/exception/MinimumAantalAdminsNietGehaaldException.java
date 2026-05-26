package be.vdab.gebruikersbeheer.derden.exception;

public class MinimumAantalAdminsNietGehaaldException extends ApplicationException {
	public MinimumAantalAdminsNietGehaaldException(int minDomainAdmins) {
		super("Je moet minstens %s administrator aanduiden.".formatted(minDomainAdmins));
	}
}
