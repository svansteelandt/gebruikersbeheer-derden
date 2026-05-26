package be.vdab.gebruikersbeheer.derden.criteria;

/**
 * SearchCriteria is an object that holds the search criteria&#46; <br>
 * The SearchCriteria is managed by the SearchCriteriaBuilder to provide a flexible design&#46;
 * 
 * @see SearchCriteriaBuilder
 */
public class SearchCriteria {

	private static final String EMPTY = "";

	private String category;
	private String filter;
	private String distinguishedName;
	private String profileName;
	private int scope;
	private int sortOrder;

	/**
	 * Get category from criteria
	 * 
	 * @return String
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Set category to criteria
	 * 
	 * @param category
	 *            category to search for
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Get filter from criteria
	 * 
	 * @return String filter
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * Set filter to criteria
	 * 
	 * @param filter
	 *            filter to set
	 */
	public void setFilter(String filter) {
		this.filter = filter;
	}

	/**
	 * Get distinguished name from criteria
	 * 
	 * @return String distinguished name
	 */
	public String getDistinguishedName() {
		return distinguishedName;
	}

	/**
	 * Set distinguished name to criteria
	 * 
	 * @param distinguishedName
	 *            distinguished name to set
	 */
	public void setDistinguishedName(String distinguishedName) {
		this.distinguishedName = distinguishedName;
	}

	/**
	 * Get profile name from criteria
	 * 
	 * @return String profile name
	 */
	public String getProfileName() {
		return profileName;
	}

	/**
	 * Set profile name to criteria
	 * 
	 * @param profileName
	 *            profile name to set
	 */
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	/**
	 * Get scope from criteria
	 * 
	 * @return int scope
	 */
	public int getScope() {
		return scope;
	}

	/**
	 * Set scope to criteria
	 * 
	 * @param scope
	 *            set scope
	 */
	public void setScope(int scope) {
		this.scope = scope;
	}

	/**
	 * Get sort order from criteria
	 * 
	 * @return int sort order
	 */
	public int getSortOrder() {
		return sortOrder;
	}

	/**
	 * Set order to criteria
	 * 
	 * @param sortOrder
	 *            set sort order
	 */
	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * Constructor
	 */
	public SearchCriteria() {
		super();

		profileName = EMPTY;

		scope = -1;
		sortOrder = -1;
	}

	@Override
	public String toString() {
		StringBuilder buf= new StringBuilder(125);
		buf.append("DN: ");
		buf.append(distinguishedName);
		
		if (category != null && !"".equals(category)){
			buf.append(", Category: ");
			buf.append(category);
		}
		
		if (profileName != null && !"".equals(profileName)){
			buf.append(", Profile: ");
			buf.append(profileName);
		}
		
		buf.append(", Filter: ");
		buf.append(filter);
		buf.append(", Scope: ");
		buf.append(scope);
		buf.append(", Sort: ");
		buf.append(sortOrder);
		
		return buf.toString();
	}
}