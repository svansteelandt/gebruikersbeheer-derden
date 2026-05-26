package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model;

import be.vdab.gebruikersbeheer.derden.domain.RoleObject;

import java.util.List;
import java.util.Set;

public record VestigingDto(
		String globalId,
		String naam,
		String oeNaam,
		Set<String> samenwerkingsAkkoorden,
		List<RoleObject> rollen,
		List<String> administratorGlobalIds
) {

}
