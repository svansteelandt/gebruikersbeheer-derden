package be.vdab.gebruikersbeheer.derden.exception;

public class LoginMethodeCouldNotBeChangedException extends ApplicationException {
	public LoginMethodeCouldNotBeChangedException() {
		super("De login methode kon niet gewijzigd worden.");
	}
}
