package be.vdab.gebruikersbeheer.derden.components.passwordchange;

public interface PasswordChangeClient {

	public void sendPasswordForgottenEmail(String vdabUid, String emailAdres);
}
