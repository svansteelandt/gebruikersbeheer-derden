package be.vdab.gebruikersbeheer.derden.service.cache;

import be.vdab.gebruikersbeheer.derden.config.CacheNames;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.HashMap;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

	@InjectMocks
	CacheServiceImpl cacheService;

	@Mock
	CacheManager cacheManager;

	private Cache personCache;

	@BeforeEach
	void init() {
		personCache = new TestCache();

		when(cacheManager.getCache(CacheNames.CACHE_PERSONS)).thenReturn(personCache);
	}

	@Test
	void testAddPersonToPersonCachesIfNoCacheAvailable() {
		when(cacheManager.getCache(CacheNames.CACHE_PERSONS)).thenReturn(null);

		Dn personDn = new Dn("ergloblaid=1,ou=,ou=people");
		var personObject = new PersonObject(personDn);
		cacheService.addPersonToPersonCaches(personObject);

		verify(cacheManager).getCache(CacheNames.CACHE_PERSONS);
	}

	@Test
	void testAddPersonToPersonCaches() {
		Dn personDn = new Dn("ergloblaid=1,ou=,ou=people");
		var personObject = new PersonObject(personDn);
		cacheService.addPersonToPersonCaches(personObject);

		verify(cacheManager).getCache(CacheNames.CACHE_PERSONS);
		assertThat(personCache.get(personDn)).isNotNull();
	}

	@Test
	void testUpdatePersonInCache() {
		Dn personDn = new Dn("ergloblaid=1,ou=,ou=people");
		var personObject1 = new PersonObject(personDn);
		personObject1.setFirstName("Firstname 1");

		var personObject2 = new PersonObject(personObject1);
		personObject2.setFirstName("Firstname 2");

		cacheService.addPersonToPersonCaches(personObject1);
		cacheService.updatePersonInCache(personDn, personObject2);

		assertThat(personCache.get(personDn, PersonObject.class)).isEqualTo(personObject2);
	}

	@Test
	void testUpdatePersonNotInCache() {
		Dn personDn = new Dn("ergloblaid=1,ou=,ou=people");
		var personObject = new PersonObject(personDn);

		cacheService.updatePersonInCache(personDn, personObject);

		assertThat(personCache.get(personDn)).isNull();
	}

	@Test
	void testUpdatePersonInCacheIfNoCacheAvailable() {
		when(cacheManager.getCache(CacheNames.CACHE_PERSONS)).thenReturn(null);

		Dn personDn = new Dn("ergloblaid=1,ou=,ou=people");
		var personObject = new PersonObject(personDn);
		cacheService.updatePersonInCache(personDn, personObject);

		verify(cacheManager).getCache(CacheNames.CACHE_PERSONS);
	}

	@Test
	void testDeleteFromPersonCaches1() {
		Dn personDn = new Dn("ergloblaid=1,ou=,ou=people");
		var personObject = new PersonObject(personDn);

		cacheService.addPersonToPersonCaches(personObject);

		assertThat(personCache.get(personDn)).isNotNull();

		cacheService.deleteFromPersonCaches(personDn);

		assertThat(personCache.get(personDn)).isNull();
	}

	@Test
	void testDeleteFromPersonCaches2() {
		Dn personDn = new Dn("ergloblaid=1,ou=,ou=people");
		var personObject = new PersonObject(personDn);

		cacheService.addPersonToPersonCaches(personObject);

		assertThat(personCache.get(personDn)).isNotNull();

		cacheService.deleteFromPersonCaches(personObject);

		assertThat(personCache.get(personDn)).isNull();
	}

	private static class TestCache implements Cache {

		private HashMap<Object, ValueWrapper> data = new HashMap<>();

		@Override
		public String getName() {
			return "testCache";
		}

		@Override
		public Object getNativeCache() {
			return null;
		}

		@Override
		public @Nullable ValueWrapper get(Object key) {
			return data.get(key);
		}

		@Override
		@SuppressWarnings("unchecked")
		public @Nullable <T> T get(Object key, @Nullable Class<T> type) {
			ValueWrapper wrapper = get(key);
			if (wrapper == null) {
				return null;
			}

			Object value = wrapper.get();
			if (value == null) {
				return null;
			}

			if (type != null && !type.isInstance(value)) {
				throw new IllegalStateException(
						"Cached value is not of required type [" + type.getName() + "]: " + value
				);
			}

			return (T) value;
		}

		@Override
		public @Nullable <T> T get(Object key, Callable<T> valueLoader) {
			return null;
		}

		@Override
		public void put(@NonNull Object key, @Nullable Object value) {
			data.put(key, () -> value);
		}

		@Override
		public void evict(Object key) {
			data.remove(key);
		}

		@Override
		public void clear() {
			data.clear();
		}
	}
}