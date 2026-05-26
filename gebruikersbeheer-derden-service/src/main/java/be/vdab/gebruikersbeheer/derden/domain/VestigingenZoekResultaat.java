package be.vdab.gebruikersbeheer.derden.domain;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VestigingenZoekResultaat(@NotNull List<AdminDomainObject> vestigingen,
                                       boolean zoekCriteriaZijnTeAlgemeen) {
}
