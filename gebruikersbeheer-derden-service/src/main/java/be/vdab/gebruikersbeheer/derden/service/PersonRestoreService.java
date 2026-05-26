package be.vdab.gebruikersbeheer.derden.service;

public interface PersonRestoreService {
	/**
	 * @param personGlobalId
	 * @return true indien restored
	 */
	boolean restore(String personGlobalId);
}
