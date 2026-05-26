package be.vdab.gebruikersbeheer.derden.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;

import java.util.Locale;

@RequiredArgsConstructor
@Slf4j
public class VDABMessageResource extends ReloadableResourceBundleMessageSource {

	private final Environment env;

	@Override
	protected String resolveCodeWithoutArguments(String code, Locale locale) {
		String value= env.getProperty(code);

		if (StringUtils.isNotEmpty(value)){
			return value;
		}

		if (log.isDebugEnabled()){
			log.debug("resolveCodeWithoutArguments {} / --> {}", code, locale, super.resolveCodeWithoutArguments(code, locale));
		}

		return super.resolveCodeWithoutArguments(code, locale);
	}

	@Override
	protected String getMessageInternal(String code, Object[] args, Locale locale) {
		log.debug("getMessageInternal: {}, {}, {}", code, args, locale);

		if (StringUtils.isEmpty(code)){
			return null;
		}

		String value= env.getProperty(code);

		if (StringUtils.isNotEmpty(value)){
			return value;
		}

		return super.getMessageInternal(code, args, locale);
	}
}
