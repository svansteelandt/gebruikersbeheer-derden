package be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui;

import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.mapper.PropertiesMapper;
import be.vdab.gebruikersbeheer.derden.adapter.in.rest.ui.model.PropertiesDto;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.core.CodesPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ui/config")
@RequiredArgsConstructor
public class PropertiesRestController {

	private final ApplicationProperties applicationProperties;
	private final PropertiesMapper propertiesMapper;
	private final CodesPort codesPort;

	@GetMapping
	public PropertiesDto getProperties() {
		return propertiesMapper.map(codesPort.getCVSRollen(), codesPort.getWebcursussen(), applicationProperties);
	}
}
