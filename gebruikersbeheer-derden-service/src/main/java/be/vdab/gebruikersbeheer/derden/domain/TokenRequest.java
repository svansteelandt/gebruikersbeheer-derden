package be.vdab.gebruikersbeheer.derden.domain;

import be.vdab.gebruikersbeheer.derden.extern.command.CreateDerdeCommand;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class TokenRequest {
	String insz;
	String voornaam;
	String naam;
	String email;
	String telefoon;
	String gsm;
	String ikp;
	List<String> toegangsrechten;

	public TokenRequest(CreateDerdeCommand command) {
		insz = command.getInsz();
		voornaam = command.getVoornaam();
		naam = command.getNaam();
		email = command.getEmailAddress();
		telefoon = command.getTelefoon();
		gsm = command.getGsm();
		ikp = command.getIkp();
		toegangsrechten = command.getToegangsrechten();
	}
}
