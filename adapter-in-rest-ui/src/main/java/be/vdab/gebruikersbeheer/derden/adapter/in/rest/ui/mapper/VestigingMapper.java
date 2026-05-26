package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.VestigingSummaryDto;
import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VestigingMapper {
	public List<VestigingSummaryDto> map(List<AdminDomainObject> adminDomainObjecten) {
		return adminDomainObjecten.stream()
				.map(adminDomainObject -> new VestigingSummaryDto(adminDomainObject.getGlobalId(),
						adminDomainObject.getName(),
						adminDomainObject.getCity(),
						adminDomainObject.getPostalcode(),
						adminDomainObject.getStreet(),
						adminDomainObject.getIkpIntern(),
						adminDomainObject.getKboNummer())
				).toList();
	}

	public VestigingDto map(AdminDomainObject adminDomainObject) {
		return new VestigingDto(adminDomainObject.getDn().getGlobalId(),
				adminDomainObject.getName(),
				adminDomainObject.getOeName(),
				adminDomainObject.getSamakks(),
				adminDomainObject.getRoles(),
				adminDomainObject.getAdministrators().stream().filter(p -> !p.isVirtualAccount()).map(a -> a.getDn().getGlobalId()).toList()
		);
	}
}
