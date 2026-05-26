package be.vdab.gebruikersbeheer.derden;

import be.vdab.gebruikersbeheer.derden.adapter.out.rest.client.codes.api.GenericRestServiceApi;
import be.vdab.gebruikersbeheer.derden.config.ApplicationProperties;
import be.vdab.gebruikersbeheer.derden.core.CodesPort;
import be.vdab.gebruikersbeheer.derden.domain.PersonObject;
import be.vdab.gebruikersbeheer.derden.intern.validator.SearchFormValidator;
import be.vdab.gebruikersbeheer.derden.security.IsimUserContextManager;
import be.vdab.gebruikersbeheer.derden.service.AccountService;
import be.vdab.gebruikersbeheer.derden.service.AdminDomainService;
import be.vdab.gebruikersbeheer.derden.service.PersonChangeRolesService;
import be.vdab.gebruikersbeheer.derden.service.PersonCreateService;
import be.vdab.gebruikersbeheer.derden.service.PersonRestoreService;
import be.vdab.gebruikersbeheer.derden.service.PersonSearchService;
import be.vdab.gebruikersbeheer.derden.service.PersonService;
import be.vdab.gebruikersbeheer.derden.service.RoleService;
import be.vdab.gebruikersbeheer.derden.util.ValidateUtils;
import be.vdab.gebruikersbeheer.derden.util.isim.support.IsimSessionService;
import be.vdab.gebruikersbeheer.util.common.domain.Dn;
import be.vdab.gebruikersbeheer.util.isim.client.IsimWsClient;
import be.vdab.testcontainers.VdabContainers;
import be.vdab.testcontainers.oracle.VdabOracleContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SpringBootWebApplication.class)
@EnableAutoConfiguration
public abstract class BaseIT {

	@MockitoBean
	protected AdminDomainService adminDomainService;

	@MockitoBean
	protected PersonService personService;

	@MockitoBean
	protected PersonCreateService personCreateService;

	@MockitoBean
	protected PersonChangeRolesService personChangeRolesService;

	@MockitoBean
	protected PersonRestoreService personRestoreService;

	@MockitoBean
	protected AccountService accountService;

	@MockitoBean
	protected RoleService roleService;

	@MockitoBean
	protected GenericRestServiceApi genericRestServiceApi;

	@MockitoBean
	protected PersonSearchService personSearchService;

	@Autowired
	protected IsimUserContextManager isimUserContextManager;

	@Autowired
	protected SearchFormValidator searchFormValidator;

	@Autowired
	protected ValidateUtils validateUtils;

	@MockitoBean
	protected IsimSessionService isimSessionService;

	@MockitoBean
	protected IsimWsClient isimWsClient;

	@Autowired
	protected CacheManager cacheManager;

	@Autowired
	protected ApplicationProperties applicationProperties;

	@Autowired
	protected RestTemplate restTemplate;

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected CodesPort codesPort;

	protected static final VdabOracleContainer vdabOracleContainer;

	static {
		vdabOracleContainer = new VdabOracleContainer();

		VdabContainers
				.register(vdabOracleContainer)
				.start();
	}

	@DynamicPropertySource
	static void initializeOracleDataSource(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("spring.datasource.url", vdabOracleContainer::getJdbcUrl);
		log.info("Testcontainer url is: {}", vdabOracleContainer.getJdbcUrl());
	}

	protected PersonObject initIsimUser() {
		return initIsimUser("USERNAME");
	}

	protected PersonObject initIsimUser(String gebruikersnaam) {
		String personDn = "erglobalid=456,ou=0,ou=persons";

		PersonObject personObject = new PersonObject();
		personObject.setUserId(gebruikersnaam);
		personObject.setVdabUid(gebruikersnaam);
		personObject.setDn(new Dn(personDn));
		when(personService.findPersonByGebruikersnaam(anyString(), anyList())).thenReturn(personObject);

		return personObject;
	}
}
