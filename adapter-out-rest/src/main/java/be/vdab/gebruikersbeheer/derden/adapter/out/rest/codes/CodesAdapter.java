package be.vdab.gebruikersbeheer.derden.adapter.out.rest.codes;

import be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.api.GenericRestServiceApi;
import be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.model.Codes;
import be.vdab.gebruikersbeheer.derden.adapter.out.rest.config.RestCacheNames;
import be.vdab.gebruikersbeheer.derden.core.CodesPort;
import be.vdab.gebruikersbeheer.derden.core.domain.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CodesAdapter implements CodesPort {

	private final GenericRestServiceApi genericRestServiceApi;

	@Override
	@Cacheable(RestCacheNames.CACHE_CVS_ROLLEN)
	public List<Code> getCVSRollen() {
		return getCodes("CVSROL");
	}

	@Override
	@Cacheable(RestCacheNames.CACHE_WEBCURSUSSEN)
	public List<Code> getWebcursussen() {
		return getCodes("WEBCURS");
	}

	@Override
	@Cacheable(RestCacheNames.CACHE_TIM_ROLLEN)
	public List<String> getAdditionalTimRollen() {
		return WebServiceConverter.filterAndGetCodeValues(
				getCodes("TIMROL"),
				code -> StringUtils.startsWith(code.getWaarde(), "AR-EXTRANET-")
						|| StringUtils.equals(code.getWaarde(), "AR-KIOSK-PARTNER")
		);
	}

	private List<Code> getCodes(String prefix) {
		Codes codes = this.genericRestServiceApi.geefCodes43(prefix, null, null, null, null);

		if (codes == null) {
			return Collections.emptyList();
		}

		return WebServiceConverter.convert(codes.getCode());
	}
}
