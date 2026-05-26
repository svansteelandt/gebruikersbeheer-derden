package be.vdab.gebruikersbeheer.derden.exception;

public abstract class NotFoundException extends RuntimeException {

	protected NotFoundException() {
	}

	protected NotFoundException(String message) {
		super(message);
	}
}
