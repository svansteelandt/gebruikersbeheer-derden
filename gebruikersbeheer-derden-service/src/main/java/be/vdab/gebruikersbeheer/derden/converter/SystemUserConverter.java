package be.vdab.gebruikersbeheer.derden.converter;

import be.vdab.gebruikersbeheer.derden.domain.SystemUserObject;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.util.isim.domain.IsimSystemUser;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SystemUserConverter extends BaseConverter implements Converter<IsimSystemUser, SystemUserObject> {

	public SystemUserConverter(RoleService roleService) {
		super(roleService);
	}

	public SystemUserObject convert(@NonNull IsimSystemUser systemUser){
			log.debug("convert {}", systemUser.getUid());

		SystemUserObject systemUserObject = new SystemUserObject();
		systemUserObject.setOwner(systemUser.getOwner());
		systemUserObject.setIsimDn(systemUser.getDn());

		return systemUserObject;
	}
}
