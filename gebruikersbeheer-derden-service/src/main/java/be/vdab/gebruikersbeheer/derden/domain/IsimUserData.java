package be.vdab.gebruikersbeheer.derden.domain;

import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class IsimUserData {

	@Setter
	private Dn isimAccountDn;
	private Dn personDn;
	private String insz;
	private String hoofdGebruikersnaam;
}
