package be.vdab.gebruikersbeheer.derden.converter;

import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.util.common.BackendAbstraction;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsAbstraction;
import be.vdab.gebruikersbeheer.util.isim.constants.IsimAttributeNames;
import com.ibm.itim.ws.model.person.WSPerson;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PersonDeconverter {

	public void convert(PersonObject personObject, BackendAbstraction.Builder<IsimWsAbstraction<WSPerson>> builder) {
		log.debug("convert {}", personObject);
		builder.attribute(IsimAttributeNames.ATTR_SN, personObject.getLastName());
		builder.attribute(IsimAttributeNames.ATTR_CN, personObject.getFullName());
		builder.attribute(IsimAttributeNames.ATTR_UID, personObject.getUserId());
		builder.attribute(IsimAttributeNames.ATTR_VDABUID, personObject.getVdabUid());
		builder.attribute(IsimAttributeNames.ATTR_GIVENNAME, personObject.getFirstName());
		builder.attribute(IsimAttributeNames.ATTR_MAIL, personObject.getEmailAddress());
		builder.attribute(IsimAttributeNames.ATTR_RIJKSREGISTERNUMMER, personObject.getNationalNumber());
		builder.attribute(IsimAttributeNames.ATTR_TELEPHONENUMBER, personObject.getPhone());
		builder.attribute(IsimAttributeNames.ATTR_MOBILE, personObject.getMobile());
		builder.attribute(IsimAttributeNames.ATTR_EMPLOYEENUMBER, personObject.getEmployeenumber() + "");
		builder.attribute(IsimAttributeNames.ATTR_AUTHENTICATIONLEVEL, personObject.getLoginMethod());
		builder.attribute(IsimAttributeNames.ATTR_VDAB_SUSPEND, personObject.isSuspend() ? "true" : "false");
		builder.attribute(IsimAttributeNames.ATTR_VDAB_SUSPENDDESCRIPTION, personObject.getSuspendOmschrijving());
		builder.attribute(IsimAttributeNames.ATTR_VDAB_CVSROLE, personObject.getVdabCvsRol());
	}
}
