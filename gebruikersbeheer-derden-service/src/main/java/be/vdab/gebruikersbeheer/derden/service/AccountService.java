package be.vdab.gebruikersbeheer.derden.service;

public interface AccountService {

	boolean tamAccountExistsAndIsDisabledForPerson(String personDn);

	void resetPassword(String vdabUid, String emailAddress);
}
