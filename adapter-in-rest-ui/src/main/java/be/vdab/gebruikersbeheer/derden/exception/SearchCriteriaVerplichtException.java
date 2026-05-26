package be.vdab.gebruikersbeheer.derden.exception;

public class SearchCriteriaVerplichtException extends ApplicationException {

	public SearchCriteriaVerplichtException() {
		super("Gelieve zoekcriteria op te geven");
	}
}
