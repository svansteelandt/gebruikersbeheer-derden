package be.vdab.gebruikersbeheer.derden.exception;

public class BackendErrorException extends RuntimeException {

	public BackendErrorException(String message) {
		super(message);
	}

	public BackendErrorException(Throwable cause) {
		super(cause);
	}
}
