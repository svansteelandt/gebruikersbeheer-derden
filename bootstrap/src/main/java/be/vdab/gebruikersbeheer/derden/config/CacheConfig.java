package be.vdab.gebruikersbeheer.derden.config;


import be.vdab.gebruikersbeheer.derden.adapter.out.rest.config.RestCacheNames;
import be.vdab.gebruikersbeheer.util.config.GebruikerBeheerUtilCacheNames;
import com.hazelcast.config.AttributeConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.config.MapConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.hazelcast.HazelcastIndexedSessionRepository;
import org.springframework.session.hazelcast.PrincipalNameExtractor;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

@Configuration
@EnableCaching
@EnableHazelcastHttpSession(maxInactiveIntervalInSeconds = 900)
public class CacheConfig {

	private final ApplicationProperties applicationProperties;

	public CacheConfig(ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}


	@Bean
	public Config hazelcastConfig() {
		Config config = Config.load()
				.setClusterName("%s-%s".formatted(applicationProperties.getApplicationName(), getHazelcastVersion()));

		initCaches(config);
		return config;
	}

	private void initCaches(Config config) {
		config.getMapConfigs().put("default", initializeDefaultMapConfig());

		config.getMapConfigs().put(GebruikerBeheerUtilCacheNames.ISIM_ROLE_BY_NAME, initializeRollenMapConfig());
		config.getMapConfigs().put(GebruikerBeheerUtilCacheNames.ISIM_ROLE_BY_DN, initializeRollenMapConfig());
		config.getMapConfigs().put(GebruikerBeheerUtilCacheNames.ISIM_DYNAMIC_ROLE_DNS, initializeRollenMapConfig());

		config.getMapConfigs().put(CacheNames.CACHE_ADMINDOMAIN, initializeAdminDomainsMapConfig());
		config.getMapConfigs().put(CacheNames.CACHE_ADMINDOMAINS, initializeAdminDomainsMapConfig());
		config.getMapConfigs().put(CacheNames.CACHE_PERSONS, initializePersonsMapConfig());
		config.getMapConfigs().put(CacheNames.CACHE_ROLES, initializeRollenMapConfig());
		config.getMapConfigs().put(CacheNames.CACHE_SYSTEMUSERS, initializeSystemUsersMapConfig());
		config.getMapConfigs().put(GebruikerBeheerUtilCacheNames.CACHE_LTPA_TOKENS, initializeLTPATokensMapConfig());

		config.getMapConfigs().put(RestCacheNames.CACHE_CVS_ROLLEN, initializeCodesMapConfig());
		config.getMapConfigs().put(RestCacheNames.CACHE_WEBCURSUSSEN, initializeCodesMapConfig());
		config.getMapConfigs().put(RestCacheNames.CACHE_TIM_ROLLEN, initializeCodesMapConfig());
		config.getMapConfigs().put(RestCacheNames.CACHE_SAMAKK_TIM_ROLLEN, initializeCodesMapConfig());

		config.getMapConfigs().put(CacheNames.CACHE_OE_NAMEN, initializeDefaultMapConfig());

		config.getMapConfigs().put(CacheNames.CACHE_TOKEN_EXCHANGE, initializeTokenExchangeMapConfig());

		AttributeConfig attributeConfig = new AttributeConfig()
				.setName(HazelcastIndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE)
				.setExtractorClassName(PrincipalNameExtractor.class.getName());

		config.getMapConfig(HazelcastIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME)
				.addAttributeConfig(attributeConfig)
				.addIndexConfig(new IndexConfig(IndexType.HASH, HazelcastIndexedSessionRepository.PRINCIPAL_NAME_ATTRIBUTE));
	}


	private MapConfig initializeDefaultMapConfig() {
		MapConfig mapConfig = new MapConfig();
		mapConfig.setBackupCount(0);
		mapConfig.setReadBackupData(true);
		// default 1 uur
		mapConfig.setTimeToLiveSeconds(60 * 60);

		return mapConfig;
	}

	private MapConfig initializeTokenExchangeMapConfig() {
		MapConfig mapConfig = initializeDefaultMapConfig();
		mapConfig.setTimeToLiveSeconds(10 * 60);

		return mapConfig;
	}

	private MapConfig initializeAdminDomainsMapConfig() {
		MapConfig mapConfig = initializeDefaultMapConfig();
		mapConfig.setTimeToLiveSeconds(applicationProperties.getHazelCastAdminDomainsConfigTtl());

		return mapConfig;
	}

	private MapConfig initializePersonsMapConfig() {
		MapConfig mapConfig = initializeDefaultMapConfig();
		mapConfig.setTimeToLiveSeconds(applicationProperties.getHazelCastPersonsConfigTtl());

		return mapConfig;
	}

	private MapConfig initializeRollenMapConfig() {
		MapConfig mapConfig = initializeDefaultMapConfig();
		mapConfig.setTimeToLiveSeconds(applicationProperties.getHazelCastRollenConfigTtl());

		return mapConfig;
	}

	private MapConfig initializeSystemUsersMapConfig() {
		MapConfig mapConfig = initializeDefaultMapConfig();
		mapConfig.setTimeToLiveSeconds(applicationProperties.getHazelCastSystemUsersConfigTtl());

		return mapConfig;
	}

	private MapConfig initializeLTPATokensMapConfig() {
		MapConfig mapConfig = initializeDefaultMapConfig();
		mapConfig.setTimeToLiveSeconds(applicationProperties.getHazelCastLTPATokensConfigTtl());

		return mapConfig;
	}

	private MapConfig initializeCodesMapConfig() {
		MapConfig mapConfig = initializeDefaultMapConfig();
		mapConfig.setTimeToLiveSeconds(applicationProperties.getHazelCastCodesConfigTtl());

		return mapConfig;
	}

	private String getHazelcastVersion() {
		return Config.class.getPackage().getSpecificationVersion();
	}
}

