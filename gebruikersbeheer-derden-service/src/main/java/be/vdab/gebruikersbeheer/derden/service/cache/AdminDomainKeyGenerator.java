package be.vdab.gebruikersbeheer.derden.service.cache;

import be.vdab.gebruikersbeheer.derden.domain.AdminDomainObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("adminDomainKeyGenerator")
@Slf4j
public class AdminDomainKeyGenerator extends SimpleKeyGenerator {

	@Override
	public Object generate(Object target, Method method, Object... params) {
		if (params.length > 0) {
			if (params.length == 1){
				// erGlobalId
				return getKey(params);
			}else{
				log.debug("Generate adminDomainKeyGenerator for method {} and params {}", method.getName(), params);
				return new SimpleKey(method.getName(), params);
			}
		} else {
			log.debug("Generate adminDomainKeyGenerator for method {} and no params", method.getName());

			return new SimpleKey(method.getName());
		}
	}

	public Object getKey(Object... params){
		if (params.length == 1){
			if (params[0] != null && params[0] instanceof AdminDomainObject object) {
				return object.getDn();
			}
		}

		return null;
	}
}
