package be.vdab.gebruikersbeheer.derden.converter;

import be.vdab.gebruikersbeheer.derden.domain.OrganizationalUnitObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import com.ibm.itim.dataservices.model.domain.OrganizationalUnit;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class OrganizationalUnitConverter implements Converter<OrganizationalUnit, OrganizationalUnitObject> {

	/**
	 * Converts organizational unit (com.ibm.itim.dataservices.model.domain.OrganizationalUnit) into <br>
	 * organizational unit (be.vdab.tim.model.OrganizationalUnitObject)
	 *
	 *            object to convert
	 * @return Object converted object
	 */
	public OrganizationalUnitObject convert(@NonNull OrganizationalUnit organizationalUnit) {
		log.info("organizationalUnitConverter.java - convert {}", organizationalUnit);

		OrganizationalUnitObject organizationalUnitObject = new OrganizationalUnitObject(Dn.of(organizationalUnit.getDistinguishedName()));
		organizationalUnitObject.setName(organizationalUnit.getName());
		return organizationalUnitObject;
	}

	public List<OrganizationalUnitObject> convertList(Collection<OrganizationalUnit> organizationalUnits) {
		log.info("organizationalUnitConverter.java - convertList");

		List<OrganizationalUnitObject> listOrganizationalUnitObject = new ArrayList<>();
		for (OrganizationalUnit OrganizationalUnit : organizationalUnits) {
			listOrganizationalUnitObject.add(convert(OrganizationalUnit));
		}

		return listOrganizationalUnitObject;
	}
}