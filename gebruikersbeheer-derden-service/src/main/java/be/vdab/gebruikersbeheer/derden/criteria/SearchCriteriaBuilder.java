package be.vdab.gebruikersbeheer.derden.criteria;

/**
 * 
 * @see SearchCriteria
 */
public class SearchCriteriaBuilder {

	private SearchCriteria searchCriteria;

	/**
	 * Get search criteria object&#46;
	 * 
	 * @return SearchCriteria search criteria object
	 */
	public SearchCriteria getSearchCriteria() {
		return searchCriteria;
	}

	/**
	 * Appends category to criteria builder&#46;
	 * 
	 * @param category
	 *            category to append to builder
	 * @return SearchCriteriaBuilder search criteria builder
	 */
	public SearchCriteriaBuilder appendCategory(String category) {
		searchCriteria.setCategory(category);
		return this;
	}

	/**
	 * Appends filter to criteria builder&#46;
	 * 
	 * @param filter
	 *            filter to append to builder
	 * @return SearchCriteriaBuilder search criteria builder
	 */
	public SearchCriteriaBuilder appendFilter(String filter) {
		searchCriteria.setFilter(filter);
		return this;
	}

	/**
	 * Appends distinguished name to criteria builder&#46;
	 * 
	 * @param distinguishedName
	 *            distinguished name to append to builder
	 * @return SearchCriteriaBuilder search criteria builder
	 */
	public SearchCriteriaBuilder appendDistinguishedName(String distinguishedName) {
		searchCriteria.setDistinguishedName(distinguishedName);
		return this;
	}

	/**
	 * Appends profile name to criteria builder&#46;
	 * 
	 * @param profileName
	 *            profile name to append to builder
	 * @return SearchCriteriaBuilder search criteria builder
	 */
	public SearchCriteriaBuilder appendProfileName(String profileName) {
		searchCriteria.setProfileName(profileName);
		return this;
	}

	/**
	 * Appends sort order to criteria builder&#46;
	 * 
	 * @param sortOrder
	 *            sort order to append to builder
	 * @return SearchCriteriaBuilder search criteria builder
	 */
	public SearchCriteriaBuilder appendSortOrder(int sortOrder) {
		searchCriteria.setSortOrder(sortOrder);
		return this;
	}

	/**
	 * Appends scope to criteria builder&#46;
	 * 
	 * @param scope
	 *            scope to append to builder
	 * @return SearchCriteriaBuilder search criteria builder
	 */
	public SearchCriteriaBuilder appendScope(int scope) {
		searchCriteria.setScope(scope);
		return this;
	}

	/**
	 * Build search criteria instance&#46;
	 * 
	 * @return SearchCriteriaBuilder search criteria builder
	 */
	public SearchCriteriaBuilder() {
		searchCriteria = new SearchCriteria();
	}
}