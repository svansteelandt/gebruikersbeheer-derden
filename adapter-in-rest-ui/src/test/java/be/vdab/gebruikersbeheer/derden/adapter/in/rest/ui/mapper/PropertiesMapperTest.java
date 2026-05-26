package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.CvsCodeDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.PropertiesDto;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.WebCursusCodeDto;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.core.domain.Code;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper.PropertiesMapper.CREATE_MLP_GEBRUIKER_GEBRUIKERSNAAM_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertiesMapperTest {
	@Mock
	ApplicationProperties applicationProperties;
	@InjectMocks
	PropertiesMapper propertiesMapper;

	@Test
	@DisplayName("""
			GIVEN application properties
			WHEN map is called
			THEN properties DTO is returned
			""")
	void mapProperties() {
		var cvsRollen = createCvsRollen();
		var webCursussen = createWebCursussen();
		when(applicationProperties.getIdentitytoolApplicationUrl()).thenReturn("http://test.org");
		var expectedDto = new PropertiesDto(applicationProperties.getIdentitytoolApplicationUrl() + CREATE_MLP_GEBRUIKER_GEBRUIKERSNAAM_PATH,
				cvsRollen.stream().filter(Code::isActief).map(rol -> new CvsCodeDto(rol.getWaarde(), rol.getLangLabel())).toList(),
				webCursussen.stream().filter(Code::isActief).map(webCursus -> new WebCursusCodeDto(webCursus.getWaarde(), webCursus.getLangLabel()))
						.toList());
		assertThat(propertiesMapper.map(cvsRollen, webCursussen, applicationProperties)).usingRecursiveComparison().isEqualTo(expectedDto);
	}

	private static List<Code> createCvsRollen() {
		var cvsRolActief = createCode("w1");
		cvsRolActief.setActief(true);
		var cvsRolPassief = createCode("w2");
		cvsRolPassief.setActief(false);
		return List.of(cvsRolActief, cvsRolPassief);
	}

	private static Code createCode(String w) {
		var cvsRol = new Code();
		cvsRol.setWaarde(w);
		cvsRol.setLangLabel("lang");
		return cvsRol;
	}

	private static List<Code> createWebCursussen() {
		var webCursusActief = createCode("c1");
		webCursusActief.setActief(true);
		var webCursusPassief = createCode("c2");
		webCursusPassief.setActief(false);
		return List.of(webCursusActief, webCursusPassief);
	}
}
