package be.vdab.gebruikersbeheer.derden.extern.service;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class ConfirmNewDerdeCreationMessage implements Serializable {

	private Ontvanger ontvanger;
	private String communicatieType;
	private String onderwerp;
	private List<String> kanalen;
	private String modaliteit;
	private List<String> documentUuids;
	private Businessdata businessdata;

	@Data
	@Builder
	public static class Ontvanger implements Serializable {
		String email;
		String voornaam;
		String naam;
	}

	@Data
	@Builder
	public static class Businessdata implements Serializable {
		private IamMailInfo iamMailInfo;
	}

	@Data
	@Builder
	public static class IamMailInfo implements Serializable {
		private String voornaam_nieuwe_gebruiker;
		private String naam_organisatie;
		private String email_nieuw;
		private String url;
		private String geldigheidsduur_token;
	}
}
