package be.vdab.gebruikersbeheer.derden.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Code implements Serializable {

	private String waarde;
	private String kortLabel;
	private String langLabel;
	private boolean actief;

	public String getLabel() {
		return kortLabel.isBlank() ? waarde : kortLabel;
	}
}
