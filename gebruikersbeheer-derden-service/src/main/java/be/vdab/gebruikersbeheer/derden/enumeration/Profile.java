package be.vdab.gebruikersbeheer.derden.enumeration;

/**
 * Enumeration Profile
 */
public enum Profile {
	
	ACCOUNT("Account"),
	ADMIN_DOMAIN("AdminDomain"),
	BP_PERSON("BPPerson"),
	ORGANIZATIONAL_UNIT("OrganizationalUnit"),
	PERSON("VDABDerde");
	
	private String type;

	public String getType() {
		return type;
	}
	
	/**
	 * Constructor
	 * @param type category type
	 */
	private Profile(String type) {
		this.type = type;
	}

	/**
	 * Finds profile by type
	 * @param type profile type
	 * @return Profile profile or null
	 */
	public static Profile findByType(String type) {
		for (Profile profile : values()) {
			if (profile.type.equalsIgnoreCase(type)) {
				return profile;
			}
		}
		throw new IllegalArgumentException(type + " is not a valild profile type");
	}

	@Override
	public String toString() {
		return super.toString();
	}
}