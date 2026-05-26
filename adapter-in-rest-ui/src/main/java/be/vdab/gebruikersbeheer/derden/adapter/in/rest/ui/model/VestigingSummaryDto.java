package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model;

public record VestigingSummaryDto(
	String globalId,
	String naam,
	String gemeente,
	String postcode,
	String straat,
	String ikp,
	String kboNummer) {
}
