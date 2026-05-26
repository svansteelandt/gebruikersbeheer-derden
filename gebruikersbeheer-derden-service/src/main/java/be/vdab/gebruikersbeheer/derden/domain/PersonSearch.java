package be.vdab.gebruikersbeheer.derden.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Getter
@Setter
public class PersonSearch implements Serializable {

	private String rijksregisternummer;
	private String voornaam;
	private String naam;
	private String gebruikersnaam;
	private String volledigeNaam;
	private String email;
	private String oe;
	private int limit;

	public PersonSearch() {
		super();
	}

	public String getSearchFilter() {
		StringBuilder stringBuilder = new StringBuilder(250);

		boolean first= true;

		if (!StringUtils.isBlank(this.rijksregisternummer)){
			stringBuilder.append("Rijksregisternummer: ");
			stringBuilder.append(this.rijksregisternummer);

			first= false;
		}

		if (!StringUtils.isBlank(this.gebruikersnaam)){
			if (!first){
				stringBuilder.append(", ");
			}

			stringBuilder.append("Gebruikersnaam: ");
			stringBuilder.append(this.gebruikersnaam);

			first= false;
		}

		if (!StringUtils.isBlank(this.voornaam)){
			if (!first){
				stringBuilder.append(", ");
			}

			stringBuilder.append("Voornaam: ");
			stringBuilder.append(this.voornaam);

			first= false;
		}

		if (!StringUtils.isBlank(this.naam)){
			if (!first){
				stringBuilder.append(", ");
			}

			stringBuilder.append("Naam: ");
			stringBuilder.append(this.naam);

			first= false;
		}

		if (!StringUtils.isBlank(this.volledigeNaam)){
			if (!first){
				stringBuilder.append(", ");
			}

			stringBuilder.append("Volledige naam: ");
			stringBuilder.append(this.volledigeNaam);

			first= false;
		}

		if (!StringUtils.isBlank(this.email)){
			if (!first){
				stringBuilder.append(", ");
			}

			stringBuilder.append("Email: ");
			stringBuilder.append(this.email);

			first= false;
		}

		if (!StringUtils.isBlank(this.oe)){
			if (!first){
				stringBuilder.append(", ");
			}

			stringBuilder.append("OE: ");
			stringBuilder.append(this.oe);
		}

		String result = stringBuilder.toString();

		if (StringUtils.isBlank(result)) {
			return "Geen zoekopdracht meegegeven.";
		}

		return result;
	}

	public boolean hasSearchCriteria() {
		return StringUtils.isNotBlank(rijksregisternummer) ||
				StringUtils.isNotBlank(voornaam) ||
				StringUtils.isNotBlank(naam) ||
				StringUtils.isNotBlank(gebruikersnaam) ||
				StringUtils.isNotBlank(volledigeNaam) ||
				StringUtils.isNotBlank(email) ||
				StringUtils.isNotBlank(oe);
	}
}
