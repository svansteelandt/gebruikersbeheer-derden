package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper.PropertiesMapper;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.PropertiesDto;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.core.CodesPort;
import be.vdab.gebruikersbeheer.derden.core.domain.Code;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertiesRestControllerTest {
	@Mock
	PropertiesMapper propertiesMapper;
	@Mock
	PropertiesDto propertiesDto;
	@Mock
	ApplicationProperties applicationProperties;
	@Mock
	CodesPort codesPort;
	@InjectMocks
	PropertiesRestController propertiesRestController;

	@Test
	@DisplayName("""
			GIVEN application properties are mapped by the mapper
			WHEN getProperties is called
			THEN properties DTO is returned
			""")
	void getProperties() {
		var cvsRol = new Code();
		var cvsRollen = List.of(cvsRol);
		var webCursus = new Code();
		var webCursussen = List.of(webCursus);

		when(codesPort.getCVSRollen()).thenReturn(cvsRollen);
		when(codesPort.getWebcursussen()).thenReturn(webCursussen);
		when(propertiesMapper.map(cvsRollen, webCursussen, applicationProperties)).thenReturn(propertiesDto);
		assertThat(propertiesRestController.getProperties()).isEqualTo(propertiesDto);
	}
}
