package be.vdab.gebruikersbeheer.derden.exception;

public class PersonRoleAssignmentNotYetFinishedException extends RuntimeException {
	public PersonRoleAssignmentNotYetFinishedException() {
		super("Het toekennen van de rollen is nog niet voltooid, gelieve zelf de pagina te herladen tot de toegekende rollen beschikbaar zijn");
	}
}
