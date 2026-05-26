package be.vdab.gebruikersbeheer.derden.exception;


import be.vdab.gebruikersbeheer.util.exception.IsimRuntimeException;

import java.io.Serial;

public class ITIMContextManagerException extends IsimRuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public ITIMContextManagerException() {
		this("ISIM context manager exception: ", null);
	}

	/**
	 * Constructor
	 * @param message exception message
	 * @param cause exception cause
	 */
	public ITIMContextManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor
	 * @param message exception message
	 */
	public ITIMContextManagerException(String message) {
		this(message, null);
	}

	/**
	 * Constructor
	 * @param cause exception cause
	 */
	public ITIMContextManagerException(Throwable cause) {
		this("ISIM context manager exception: ", cause);
	}
}
