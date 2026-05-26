package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model;

import be.vdab.gebruikersbeheer.derden.domain.LoginMethod;

import java.util.List;

public record GebruikerSummaryDto(
		String globalId,
		String userId,
		String vdabUid,
		String fullName,
		String profileName,
		String displayRole,
		boolean suspend,
		LoginMethod loginMethod,
		List<String> roleIds,
		String bedrijfsNaam,
		String ikp,
		GebruikerStatus status,
		String deleteDescription,
		String vestigingId,
		String email) {
}
