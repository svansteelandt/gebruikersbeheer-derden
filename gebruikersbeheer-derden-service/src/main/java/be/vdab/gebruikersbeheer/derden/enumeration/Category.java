package be.vdab.gebruikersbeheer.derden.enumeration;

/**
 * Enumeration Category
 */
public enum Category {
	
	ACCOUNT("Account"),
	VDABDerde("VDABDerde"), 
	BP_UNIT("BusinessPartnerOrganization"),
	GLOBAL("Global"),
	LOCATION("Location"),
	ORGZANIZATION("Organization"),
	ORGANIZATION_UNIT("OrganizationUnit"),
	PERSON("Person"),
	ROLE("Role"),
	SECURITY_DOMAIN("SecurityDomain");
	
	private String type;

	public String getType() {
		return type;
	}
	
	/**
	 * Constructor
	 * @param type category type
	 */
	private Category(String type) {
		this.type = type;
	}

	/**
	 * Finds category by type
	 * @param type category type
	 * @return Category category or null
	 */
	public static Category findByType(String type) {
		for (Category category : values()) {
			if (category.type.equalsIgnoreCase(type)) {
				return category;
			}
		}
		throw new IllegalArgumentException(type + " is not a valild category type");
	}

	@Override
	public String toString() {
		return super.toString();
	}
}