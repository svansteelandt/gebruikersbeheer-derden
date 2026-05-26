package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model;

import java.util.List;

public record AangelogdeGebruikerDto(String username,
                                     String volledigeNaam,
                                     List<String> rollen) {
}
