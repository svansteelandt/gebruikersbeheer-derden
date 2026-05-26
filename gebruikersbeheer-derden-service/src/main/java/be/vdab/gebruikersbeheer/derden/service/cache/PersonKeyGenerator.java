package be.vdab.gebruikersbeheer.derden.service.cache;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("personKeyGenerator")
@Slf4j
public class PersonKeyGenerator extends SimpleKeyGenerator {

	@Override
	public Object generate(Object target, Method method, Object... params) {
		if (params.length > 0) {
			if (params.length <= 2) {
				// erGlobalId
				return getKey(params);
			} else {
				log.debug("Generate personkey for method {} and params {}", method.getName(), params);
				return new SimpleKey(method.getName(), params);
			}
		} else {
			log.debug("Generate personkey for method {} and no params", method.getName());
			return new SimpleKey(method.getName());
		}
	}

	public Object getKey(Object... params) {
		if (params.length == 1) {
			if (params[0] != null) {
				return params[0];
			}
		} else if (params.length == 2) {
			if (params[0] != null && (params[1] == null || params[1] instanceof AdminDomainObject)) {
				return params[0];
			}
		}

		return null;
	}
}
