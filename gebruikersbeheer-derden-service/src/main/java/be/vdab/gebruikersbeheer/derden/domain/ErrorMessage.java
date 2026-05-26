package be.vdab.gebruikersbeheer.derden.domain;

import java.util.UUID;

public class ErrorMessage {

	private UUID uuid;
	private String key;
	private String message;

	public ErrorMessage(String key, String message) {
		this.uuid = UUID.randomUUID();
		this.key = key;
		this.message = message;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getKey() {
		return key;
	}

	public String getMessage() {
		return message;
	}
}
