package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.CvsCodeDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.PropertiesDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.WebCursusCodeDto;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.core.domain.Code;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PropertiesMapper {

	public static final String CREATE_MLP_GEBRUIKER_GEBRUIKERSNAAM_PATH = "/create-mlp-gebruiker?gebruikersnaam=";

	public PropertiesDto map(List<Code> cvsRollen,
	                         List<Code> webCursussen,
	                         ApplicationProperties applicationProperties) {
		return new PropertiesDto(
				applicationProperties.getIdentitytoolApplicationUrl() +
						CREATE_MLP_GEBRUIKER_GEBRUIKERSNAAM_PATH,
				cvsRollen.stream().filter(Code::isActief)
						.map(cvsRol -> new CvsCodeDto(cvsRol.getWaarde(), cvsRol.getLangLabel())).toList(),
				webCursussen.stream().filter(Code::isActief)
						.map(webCursus -> new WebCursusCodeDto(webCursus.getWaarde(), webCursus.getLangLabel()))
						.toList());
	}
}
