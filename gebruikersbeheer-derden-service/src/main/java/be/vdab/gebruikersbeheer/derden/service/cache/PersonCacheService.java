package be.vdab.gebruikersbeheer.derden.service.cache;

import org.springframework.web.bind.annotation.PathVariable;

public interface PersonCacheService {
	void clearVestigingCache(@PathVariable() String globalId);
}
