package be.vdab.gebruikersbeheer.derden.components.codes.webservice;

import be.vdab.gebruikersbeheer.derden.BaseIT;
import be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.model.Codes;
import be.vdab.gebruikersbeheer.derden.adapter.out.rest.config.RestCacheNames;
import be.vdab.gebruikersbeheer.derden.core.domain.Code;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class CodesPortIT extends BaseIT {

	@BeforeEach
	void clearCache() {
		Stream.of(RestCacheNames.CACHE_CVS_ROLLEN,
						RestCacheNames.CACHE_WEBCURSUSSEN,
						RestCacheNames.CACHE_TIM_ROLLEN
				)
				.map(cacheManager::getCache)
				.filter(Objects::nonNull)
				.forEach(Cache::clear);
	}

	@Test
	@DisplayName("""
			WHEN code service give a list of cvs roles
			THEN returns list of cvs roles
			""")
	void canFetchCvsRollen() {
		Codes codes = new Codes();
		codes.getCode().add(createCode("MLP_PRESTATIES"));
		codes.getCode().add(createCode("MLP_OCMW_PRESTATIES"));
		codes.getCode().add(createCode("MLP_BEGELEIDING"));

		String code = "CVSROL";
		when(this.genericRestServiceApi.geefCodes43(code, null, null, null, null))
				.thenReturn(codes);

		List<Code> cvsRollen = codesPort.getCVSRollen();
		assertThat(cvsRollen)
				.hasSize(3)
				.allMatch(codeObject -> codeObject.getWaarde().startsWith("MLP_"));
	}

	@Test
	@DisplayName("""
			WHEN code service give a list of webcurssusen
			THEN returns list of webcurssusen
			""")
	void canFetchWebCursussen() {
		Codes codes = new Codes();
		codes.getCode().add(createCode("r&d Cursus1"));
		codes.getCode().add(createCode("r&d Cursus2"));
		codes.getCode().add(createCode("r&d Cursus3"));

		String code = "WEBCURS";

		when(this.genericRestServiceApi.geefCodes43(code, null, null, null, null))
				.thenReturn(codes);

		List<Code> webcursussen = codesPort.getWebcursussen();
		assertThat(webcursussen)
				.hasSize(3)
				.allMatch(codeObject -> codeObject.getWaarde().startsWith("r&d"));
	}

	@Test
	@DisplayName("""
			WHEN code service gives a list of roles
			THEN returns the EXTRANET roles
			""")
	void canFetchTimRollenExtranet() {
		Codes codes = new Codes();
		codes.getCode().add(createCode("AR-EXTRANET-ROLE1"));
		codes.getCode().add(createCode("AR-EXTRANET-ROLE2"));
		codes.getCode().add(createCode("AR-XXX-YYYY"));

		String code = "TIMROL";

		List<String> timRollen = getTimRollen(codes, code);

		assertThat(timRollen)
				.hasSize(2)
				.allMatch(rol -> rol.startsWith("AR-EXTRANET-"));
	}

	@Test
	@DisplayName("""
			WHEN code service gives a list of roles with Kiosk Partner role
			THEN returns the Kiosk Partner role
			""")
	void canFetchKioskPartnerRole() {
		Codes codes = new Codes();
		codes.getCode().add(createCode("AR-KIOSK-PARTNER"));
		codes.getCode().add(createCode("AR-XXX-YYYY"));

		String code = "TIMROL";

		List<String> timRollen = getTimRollen(codes, code);
		assertThat(timRollen).containsExactly("AR-KIOSK-PARTNER");
	}

	private List<String> getTimRollen(Codes codes, String code) {
		when(this.genericRestServiceApi.geefCodes43(code, null, null, null, null))
				.thenReturn(codes);

		List<String> timRollen = codesPort.getAdditionalTimRollen();

		return timRollen;
	}

	private be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.model.Code createCode(String waarde) {
		be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.model.Code code = new be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.model.Code();
		code.setWaarde(waarde);
		code.setActief(be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.model.Code.ActiefEnum.JA);

		return code;
	}
}