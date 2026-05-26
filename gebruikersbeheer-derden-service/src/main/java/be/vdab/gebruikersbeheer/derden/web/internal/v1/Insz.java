package be.vdab.gebruikersbeheer.derden.web.internal.v1;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Insz {

	@InszConstraint
	private String inszNummer;

	public boolean isMale() {
		int lastDigitOfVolgnummer = inszNummer.charAt(8);
		return lastDigitOfVolgnummer % 2 == 1;
	}
}
