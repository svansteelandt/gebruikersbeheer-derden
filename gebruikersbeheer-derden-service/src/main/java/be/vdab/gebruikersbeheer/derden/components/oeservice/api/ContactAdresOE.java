package be.vdab.gebruikersbeheer.derden.components.oeservice.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ContactAdresOE {

	private String naam;
}
