package be.vdab.gebruikersbeheer.derden.exception;

public class RoleNotFoundException extends NotFoundException {

	public RoleNotFoundException() {
		super();
	}

	public RoleNotFoundException(String rolName) {
		super(rolName);
	}
}
