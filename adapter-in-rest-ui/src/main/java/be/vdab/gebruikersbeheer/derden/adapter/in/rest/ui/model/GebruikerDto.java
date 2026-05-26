package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model;

import be.vdab.gebruikersbeheer.derden.domain.LoginMethod;
import be.vdab.gebruikersbeheer.derden.domain.RoleObject;
import lombok.Builder;

import java.util.List;

@Builder(builderClassName = "Builder")
public record GebruikerDto(
		String globalId,
		String vestigingId,
		String userId,
		String vdabUid,
		String fullName,
		String firstName,
		String lastName,
		String profileName,
		String displayRole,
		boolean suspend,
		LoginMethod loginMethod,
		List<RoleObject> rollen,
		boolean rijkregisterNummerIsToegankelijk,
		String rijkregisterNummer,
		String email,
		String telefoon,
		String gsm,
		String ikp,
		String bedrijfsNaam,
		String suspendOmschrijving,
		CvsCodeDto cvsRol
) {
}
