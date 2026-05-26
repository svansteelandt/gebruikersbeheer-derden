package be.vdab.gebruikersbeheer.derden.extern.service;

import java.util.Collections;

public class CreateDerdeMailCommandFactory {

	public static ConfirmNewDerdeCreationMessage createDerdeMailCommandRegular() {
		return createDerdeMailCommand("", "Dries", "Thieren", "dries@vdab.be");
	}

	public static ConfirmNewDerdeCreationMessage createDerdeMailCommandTimCopy() {
		return createDerdeMailCommand("[TIM COPY] ", "Derden", "Admin", "derden.admin@vdab.be");
	}

	private static ConfirmNewDerdeCreationMessage createDerdeMailCommand(String subjectPrefix, String emailVoornaam, String emailNaam, String emailAdres) {
		return ConfirmNewDerdeCreationMessage.builder()
				.ontvanger(ConfirmNewDerdeCreationMessage.Ontvanger.builder()
						.voornaam(emailVoornaam)
						.naam(emailNaam)
						.email(emailAdres)
						.build())
				.businessdata(ConfirmNewDerdeCreationMessage.Businessdata.builder()
						.iamMailInfo(ConfirmNewDerdeCreationMessage.IamMailInfo.builder()
								.naam_organisatie("VDAB")
								.geldigheidsduur_token("0")
								.email_nieuw("newEmail@mail.be")
								.voornaam_nieuwe_gebruiker("Dries")
								.url("https://www.werkgevers.vdab.be/gebruikersbeheer-derden/extern/person/create?token=eySchoonToken")
								.build())
						.build())
				.kanalen(Collections.singletonList("EMAIL"))
				.onderwerp(subjectPrefix + "Bevestig nieuwe toegang Mijn VDAB")
				.communicatieType("IAM_DERDEN_AANMAKEN_NIEUWE_GEBRUIKER_VIA_TOKEN")
				.build();
	}
}
