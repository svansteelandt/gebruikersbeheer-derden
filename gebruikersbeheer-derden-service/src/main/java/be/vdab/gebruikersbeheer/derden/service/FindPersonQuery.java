package be.vdab.gebruikersbeheer.derden.service;

import lombok.Builder;

@Builder
public record FindPersonQuery(String gebruikersnaam, String rijksregisternummer, String voornaam, String naam, String volledigeNaam, String email, String oe,
                              int limit) {
}
