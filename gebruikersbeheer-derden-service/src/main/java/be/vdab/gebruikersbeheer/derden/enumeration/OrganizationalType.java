package be.vdab.gebruikersbeheer.derden.enumeration;

/**
 * Enumeration OrganizationalUnit - 2 trigger modes submitting <br><br>
 * 
 * Organizational Units: <br>
 * - BURGERS <br>
 * - CONFIG <br>
 * - DERDEN <br>
 * - MEDEWERKERS <br>
 */
public enum OrganizationalType {

	BURGERS("Burgers"),
	CONFIG("Config"),
	DERDEN("Derden"),
	MEDEWERKERS("Medewerkers");
	
	private String type;

	public String getType() {
		return type.toUpperCase();
	}
	
	/**
	 * Constructor
	 * @param type organizational unit type
	 */
	private OrganizationalType(String type) {
		this.type = type;
	}
	
	/**
	 * Finds organizational unit type
	 * 
	 * @param type organizational unit type
	 * @return OrganizationalUnit organizational unit or null
	 */
	public static OrganizationalType findByType(String type) {
		for (OrganizationalType organizationalType : values()) {
			if (organizationalType.type.equalsIgnoreCase(type)) {
				return organizationalType;
			}
		}
		throw new IllegalArgumentException(type + " is not a organizational unit type");
	}

	@Override
	public String toString() {
		return super.toString();
	}
}