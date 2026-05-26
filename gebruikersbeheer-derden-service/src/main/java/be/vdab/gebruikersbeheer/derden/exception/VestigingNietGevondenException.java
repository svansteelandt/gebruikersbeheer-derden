package be.vdab.gebruikersbeheer.derden.exception;

public class VestigingNietGevondenException extends NotFoundException {
	public VestigingNietGevondenException(String globalId) {
		super("Vestiging %s kan niet gevonden worden".formatted(globalId));
	}
}
