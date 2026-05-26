package be.vdab.gebruikersbeheer.derden.exception;

public class ISIMSessionException extends RuntimeException {

    private static final String MESSAGE = "No session to isim Webservices";

    public ISIMSessionException() {
        super(MESSAGE);
    }
}
