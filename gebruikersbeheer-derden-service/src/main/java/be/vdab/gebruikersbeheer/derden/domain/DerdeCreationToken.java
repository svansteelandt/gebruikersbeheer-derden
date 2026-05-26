package be.vdab.gebruikersbeheer.derden.domain;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class DerdeCreationToken {

	String token;
	LocalDateTime expirationTime;

}
