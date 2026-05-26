package be.vdab.gebruikersbeheer.derden.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ClaimsResponse {
	String insz;
	String voornaam;
	String naam;
	String email;
	String telefoon;
	String gsm;
	String ikp;
	List<String> toegangsrechten;
}
