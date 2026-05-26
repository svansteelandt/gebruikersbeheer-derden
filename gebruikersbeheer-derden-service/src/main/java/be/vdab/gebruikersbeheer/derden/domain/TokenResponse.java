package be.vdab.gebruikersbeheer.derden.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class TokenResponse {
	String token;
	LocalDateTime expirationTime;
}
