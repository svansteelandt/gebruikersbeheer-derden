package be.vdab.gebruikersbeheer.derden.service;

import be.vdab.gebruikersbeheer.derden.exception.ApplicationException;

import java.io.Serial;

public class MinimumAdminsReachedException extends ApplicationException {

	@Serial
	private static final long serialVersionUID = 1L;

	public MinimumAdminsReachedException(int minimumAantal) {
		super("Je kan deze administrator niet verwijderen. Je moet minstens %s administrator aanduiden.".formatted(minimumAantal));
	}

}
