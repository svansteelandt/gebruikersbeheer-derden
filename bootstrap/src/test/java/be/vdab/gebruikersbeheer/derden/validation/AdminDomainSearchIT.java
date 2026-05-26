package be.vdab.gebruikersbeheer.derden.validation;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainSearch;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DirectFieldBindingResult;

class AdminDomainSearchIT extends BaseIT {

	private static final String ADMIN_NAME = "Testbedrijf";
	private static final String ADMIN_STREET = "Keizerslaan 31";
	private static final String ADMIN_POSTALCODE = "1000";
	private static final String ADMIN_CITY = "Brussel";
	private static final String ADMIN_IKP = "203000";
	private static final String ADMIN_IKP_END = "0";
	private static final String ADMIN_KBONUMMER = "0123.456.789";
	private static final String ADMIN_OE = "123";

	@Test
	void validSearchForm() {
		AdminDomainSearch adminDomainSearch = new AdminDomainSearch();
		adminDomainSearch.setName(ADMIN_NAME);
		adminDomainSearch.setStreet(ADMIN_STREET);
		adminDomainSearch.setPostalcode(ADMIN_POSTALCODE);
		adminDomainSearch.setCity(ADMIN_CITY);
		adminDomainSearch.setIkp(ADMIN_IKP);
		adminDomainSearch.setIkpEnd(ADMIN_IKP_END);
		adminDomainSearch.setKboNummer(ADMIN_KBONUMMER);
		adminDomainSearch.setOe(ADMIN_OE);

		BindingResult result = new DirectFieldBindingResult(adminDomainSearch, "AdminDomainSearch");

		searchFormValidator.validate(adminDomainSearch, result);
	}
}
