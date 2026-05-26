package be.vdab.gebruikersbeheer.derden.exception;

import org.springframework.dao.DataAccessException;

import java.io.Serial;

/**
 * ITIMTemplateException - Custom DataAccessException
 * 
 * @see DataAccessException (Spring Framework)
 */
public class ITIMTemplateException extends DataAccessException {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public ITIMTemplateException() {
		super("ITIM Template Exception", null);
	}
	
	/**
	 * Constructor
	 * 
	 * @param message exception message
	 * @param cause exception cause
	 */
	public ITIMTemplateException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Constructor
	 * 
	 * @param message exception message
	 */
	public ITIMTemplateException(String message) {
		super(message, null);
	}
	
	/**
	 * Constructor
	 * 
	 * @param cause exception cause
	 */
	public ITIMTemplateException(Throwable cause) {
		super("ITIM Template Exception", cause);
	}
}