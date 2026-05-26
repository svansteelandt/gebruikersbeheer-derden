package be.vdab.gebruikersbeheer.derden.exception;

public class GebruikerHeeftGeenEmailAdresException extends ApplicationException {
	public GebruikerHeeftGeenEmailAdresException(String personId) {
		super("Gebruiker %s heeft geen emailadres".formatted(personId));
	}
}
