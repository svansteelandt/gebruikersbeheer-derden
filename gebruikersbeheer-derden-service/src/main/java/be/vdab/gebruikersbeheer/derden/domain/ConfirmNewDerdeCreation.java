package be.vdab.gebruikersbeheer.derden.domain;

import be.vdab.gebruikersbeheer.derden.extern.command.CreateDerdeCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class ConfirmNewDerdeCreation {
	private String firstNameNewUser;
	private String lastNameNewUser;
	private String organisationName;
	private String newEmail;
	private String token;
	private LocalDateTime tokenExpirationTime;
	private Set<String> recipients;

	public ConfirmNewDerdeCreation(CreateDerdeCommand command, DerdeCreationToken tokenForDerdeCreation, List<String> recipients) {
		this.firstNameNewUser = command.getVoornaam();
		this.lastNameNewUser = command.getNaam();
		this.organisationName = command.getOrganization().getName();
		this.newEmail = command.getEmailAddress();
		this.token = tokenForDerdeCreation.getToken();
		this.tokenExpirationTime = tokenForDerdeCreation.getExpirationTime();
		this.recipients = new HashSet<>(recipients);
	}
}
