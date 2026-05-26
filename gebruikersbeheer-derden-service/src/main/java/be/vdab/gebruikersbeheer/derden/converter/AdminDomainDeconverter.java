package be.vdab.gebruikersbeheer.derden.converter;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import com.ibm.itim.common.AttributeValue;
import com.ibm.itim.dataservices.model.domain.AdminDomain;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AdminDomainDeconverter {

	public AdminDomain convert(AdminDomainObject adminDomainObject) {
		log.debug("convert {}", adminDomainObject);
		
		AdminDomain adminDomain = new AdminDomain();
		adminDomain.setDistinguishedName((adminDomainObject.getDn() != null) ? adminDomainObject.getDn().asDistinguishedName() : null);
		adminDomain.setAttribute(new AttributeValue(IsimAttributeNames.ATTR_PARENT, adminDomainObject.getParent()));
		adminDomain.setAttribute(new AttributeValue(IsimAttributeNames.ATTR_STREET, adminDomainObject.getStreet()));
		adminDomain.setAttribute(new AttributeValue(IsimAttributeNames.ATTR_ROLES, adminDomainObject.getRoles()));
		adminDomain.setAttribute(new AttributeValue(IsimAttributeNames.ATTR_CITY, adminDomainObject.getCity()));
		adminDomain.setAttribute(new AttributeValue(IsimAttributeNames.ATTR_IKP, adminDomainObject.getIkp()));
		adminDomain.setAttribute(new AttributeValue(IsimAttributeNames.ATTR_POSTCODE, adminDomainObject.getPostalcode()));
		adminDomain.setAttribute(new AttributeValue(IsimAttributeNames.ATTR_GLOBALID, adminDomainObject.getGlobalId()));
		adminDomain.setAttribute(new AttributeValue(IsimAttributeNames.ATTR_OU, adminDomainObject.getOu()));
		adminDomain.setAttribute(new AttributeValue(IsimAttributeNames.ATTR_VDAB_OE, adminDomainObject.getVdaboe()));

		List<PersonObject> personObjects = adminDomainObject.getAdministrators();
		List<Dn> administrators = new ArrayList<>();

		for (PersonObject personObject : personObjects) {
			administrators.add(personObject.getDn());
		}
		adminDomain.setAttribute(new AttributeValue(IsimAttributeNames.ATTR_ADMINSTRATOR, administrators));

		return adminDomain;
	}
}
