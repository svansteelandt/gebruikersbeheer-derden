package be.vdab.gebruikersbeheer.derden.exception;


import be.vdab.gebruikersbeheer.util.exception.IsimRuntimeException;

import java.io.Serial;

/**
 * ITIMDataAccessException - Custom RuntimeException
 *
 *	@see IsimRuntimeException
 */
public class RMIRemoteException extends IsimRuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public RMIRemoteException() {
		this("RMI remote exception: ", null);
	}

	/**
	 * Constructor
	 * @param message exception message
	 * @param cause exception cause
	 */
	public RMIRemoteException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor
	 * @param message exception message
	 */
	public RMIRemoteException(String message) {
		this(message, null);
	}

	/**
	 * Constructor
	 * @param cause exception cause
	 */
	public RMIRemoteException(Throwable cause) {
		this("RMI remote exception: ", cause);
	}
}