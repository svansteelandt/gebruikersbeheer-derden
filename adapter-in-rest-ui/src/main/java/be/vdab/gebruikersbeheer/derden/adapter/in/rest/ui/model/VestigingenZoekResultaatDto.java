package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model;

import java.util.List;

public record VestigingenZoekResultaatDto(List<VestigingSummaryDto> vestigingen, boolean tooManyResults) {
}
