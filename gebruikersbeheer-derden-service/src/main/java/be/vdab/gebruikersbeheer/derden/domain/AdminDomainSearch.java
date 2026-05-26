package be.vdab.gebruikersbeheer.derden.domain;


import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.AndFilterBuilder;
import be.vdab.gebruikersbeheer.util.isim.ldap.filter.LdapFilter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Getter
@Setter
public class AdminDomainSearch implements Serializable {
	
	private String name;
	private String street;
	private String city;
	private String postalcode;
	private String ikp;
	private String ikpEnd;
	private String kboNummer;
	private String oe;
	private int limit;

	public AdminDomainSearch() {
		super();
	}


	// Echte query
	public LdapFilter ldapFilter() {
		AndFilterBuilder filterBuilder = LdapFilter.andFilter();

		if (StringUtils.isNotBlank(name)){
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_OU, name + "*"));
		}

		if (StringUtils.isNotBlank(street)){
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_STREET, street + "*"));
		}

		if (StringUtils.isNotBlank(city)){
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_CITY, city + "*"));
		}
		
		if (StringUtils.isNotEmpty(ikp)){
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_VDAB_HOOFDZETEL, ikp));
		}
		
		if (StringUtils.isNotEmpty(ikpEnd)) {
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_VDAB_VOLGNUMMER, StringUtils.leftPad(ikpEnd, 3, '0')));
		}
		
		if (StringUtils.isNotEmpty(kboNummer)){
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_VDAB_KBO, kboNummer));
		}
		
		if (StringUtils.isBlank(ikp) && StringUtils.isBlank(ikpEnd)){
			filterBuilder.add(LdapFilter.presenceFilter(IsimAttributeNames.ATTR_IKP));
		}

		if (StringUtils.isNotEmpty(oe)){
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_VDAB_OE, oe));
		}
		
		if (StringUtils.isNotBlank(postalcode)){
			filterBuilder.add(LdapFilter.equalityFilter(IsimAttributeNames.ATTR_POSTCODE, postalcode + "*"));
		}

		return filterBuilder.build();
	}

	public boolean hasSearchCriteria() {
		// we don't take into account ikpEnd
		return StringUtils.isNotBlank(name) ||
				StringUtils.isNotBlank(street) ||
				StringUtils.isNotBlank(city) ||
				StringUtils.isNotBlank(postalcode) ||
				StringUtils.isNotBlank(ikp) ||
				StringUtils.isNotBlank(kboNummer) ||
				StringUtils.isNotBlank(oe);
	}

	// Query voor ui
	public String getSearchFilter() {
		StringBuilder stringBuilder = new StringBuilder(250);
		stringBuilder.append(StringUtils.isBlank(name) ? "" : "Naam: " + name + ", ");
		stringBuilder.append(StringUtils.isBlank(street) ? "" : "Straat: " + street + ", ");
		stringBuilder.append(StringUtils.isBlank(city) ? "" : "Gemeente: " + city + ", ");
		stringBuilder.append(StringUtils.isBlank(postalcode) ? "" : "Postcode: " + postalcode + ", ");
		stringBuilder.append(StringUtils.isBlank(ikp + ikpEnd) ? "" : "IKP-nr: " + ikp + ikpEnd + ", ");
		stringBuilder.append(StringUtils.isBlank(this.kboNummer) ? "" : "KBO-Nummer: " + this.kboNummer + ", ");
		stringBuilder.append(StringUtils.isBlank(this.oe) ? "" : "OE: " + this.oe + ", ");
		
		String result = stringBuilder.toString();

		if (result.length() >= 2) {
			result = result.substring(0, stringBuilder.length() - 2);
		}
		if (StringUtils.isBlank(result)) {
			return "Geen zoekopdracht meegegeven.";
		}
		
		return result;
	}

	@Override
	public String toString() {
		return "AdminDomainSearch %n[%nname=%s%nstreet=%s%ncity=%s%npostalcode=%s%nikp=%s%nikpEnd=%s%noe=%s]".formatted(name, street, city, postalcode, ikp, ikpEnd, oe);
	}
}