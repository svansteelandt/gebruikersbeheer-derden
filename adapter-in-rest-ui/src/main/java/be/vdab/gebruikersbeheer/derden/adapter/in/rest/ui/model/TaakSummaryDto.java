package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model;

import java.util.Date;

public record TaakSummaryDto(String id,
                             String onderwerp,
                             String vestigingNaam,
                             String aangevraagdDoor,
                             String aangevraagdVoor,
                             Date aangevraagdDatum) {
}
