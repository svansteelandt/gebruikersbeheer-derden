package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model;

import java.util.List;

public record GebruikerZoekResultaatDto(List<GebruikerSummaryDto> gebruikers, List<String> gevondenIkps, boolean tooManyResults) {
}
