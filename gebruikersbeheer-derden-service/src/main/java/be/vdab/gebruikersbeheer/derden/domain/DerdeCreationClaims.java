package be.vdab.gebruikersbeheer.derden.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class DerdeCreationClaims implements Serializable {
	String insz;
	String voornaam;
	String naam;
	String email;
	String telefoon;
	String gsm;
	String ikp;
	List<String> toegangsrechten;

	public DerdeCreationClaims(ClaimsResponse claims) {
		insz = claims.getInsz();
		voornaam = claims.getVoornaam();
		naam = claims.getNaam();
		email = claims.getEmail();
		telefoon = claims.getTelefoon();
		gsm = claims.getGsm();
		ikp = claims.getIkp();
		toegangsrechten = claims.getToegangsrechten();
	}
}
