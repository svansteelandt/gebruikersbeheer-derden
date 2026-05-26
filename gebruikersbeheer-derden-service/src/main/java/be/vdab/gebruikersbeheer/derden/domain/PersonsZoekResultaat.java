package be.vdab.gebruikersbeheer.derden.domain;

import be.vdab.gebruikersbeheer.util.common.domain.Ikp;

import java.util.List;

public record PersonsZoekResultaat(List<PersonObject> persons, List<Ikp> gevondenIkps) {
}
