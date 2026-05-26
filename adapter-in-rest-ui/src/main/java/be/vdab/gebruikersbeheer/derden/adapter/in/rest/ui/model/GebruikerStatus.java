package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model;

public enum GebruikerStatus {

	ACTIEF(0), PASSIEF(1), GESCHORST(2);

	private final int value;

	GebruikerStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static GebruikerStatus fromValue(int value) {
		for (GebruikerStatus status : values()) {
			if (status.value == value) {
				return status;
			}
		}

		throw new IllegalArgumentException("Unknown status " + value);
	}
}
