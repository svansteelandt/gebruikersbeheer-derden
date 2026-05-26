package be.vdab.gebruikersbeheer.derden.config;

import be.vdab.gebruikersbeheer.util.config.GebruikersbeheerUtilApplicationProperties;
import be.vdab.monitoring.actuator.info.ApplicationPropertiesInfoContributor;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Getter
@Component
@DependsOn({"ldapContextSource"})
@RefreshScope
public class ApplicationProperties extends GebruikersbeheerUtilApplicationProperties {

	public ApplicationProperties(Environment environment, ConfigurableEnvironment configurableEnvironment) {
		String embeddedLdapPort = environment.getProperty("local.ldap.port");
		if (embeddedLdapPort != null) {
			Properties properties = new Properties();
			properties.put("isim.ldap.port", embeddedLdapPort);
			properties.put("fgds.ldap.port", embeddedLdapPort);
			PropertySource<?> embeddedLdapPropertySource = new PropertiesPropertySource("EmbeddedLdapProperties", properties);

			configurableEnvironment.getPropertySources().addFirst(embeddedLdapPropertySource);
		}
	}

	@RefreshScope
	@Bean(name = "applicationPropertiesInfoContributor")
	public InfoContributor applicationPropertiesInfoContributor() {
		return new ApplicationPropertiesInfoContributor(this);
	}

	@Value("${spring.application.name}")
	private String applicationName;

	// 4u default
	@Value("${hazelcast.admindomains.ttl:14400}")
	private int hazelCastAdminDomainsConfigTtl;

	// 8u default
	@Value("${hazelcast.adminrole.ttl:28800}")
	private int hazelCastAdminRoleConfigTtl;

	// 4u default
	@Value("${hazelcast.admindomains_persons.ttl:14400}")
	private int hazelCastAdminDomainPersonsConfigTtl;

	// 4u default
	@Value("${hazelcast.persons.ttl:14400}")
	private int hazelCastPersonsConfigTtl;

	// 8u default
	@Value("${hazelcast.rollen.ttl:28800}")
	private int hazelCastRollenConfigTtl;

	// 4u default
	@Value("${hazelcast.systemusers.ttl:14400}")
	private int hazelCastSystemUsersConfigTtl;

	// 5 min default
	@Value("${hazelcast.ltpatokens.ttl:300}")
	private int hazelCastLTPATokensConfigTtl;

	// 1u default
	@Value("${hazelcast.codes.ttl:3600}")
	private int hazelCastCodesConfigTtl;

	@Value("${zuil}")
	private String zuil;

	@Value("${environment}")
	private String environment;

	@Value("${tim.vdab-extern.header.uri}")
	private String headerUri;

	@Value("${tim.vdab-extern.footer.uri}")
	private String footerUri;

	@Value("${infoheader.visible}")
	private String infoHeaderVisible;

	@Value("${timui.ui.maxrecords}")
	private String uiMaxRecords;

	@Value("${timui.dao.maxrecords}")
	private int daoMaxRecords;

	@Value("${logout.consulent.url}")
	private String logoutConsulentUrl;

	@Value("${logout.werkgevers.url}")
	private String logoutWerkgeversUrl;

	@Value("${service.codes.url}")
	private String codesUrl;

	@Value("${service.businessservice.url}")
	private String businessServiceUrl;

	@Value("${service.passwordchange.url}")
	private String passwordChangeUrl;

	@Value("${service.auth4u.url}")
	private String auth4uUrl;

	@Value("${service.orgeenheid.url}")
	private String oeServiceUrl;

	@Value("${service.werkgevers.extern.url}")
	private String werkgeversUrl;

	@Value("${service.website.url}")
	private String websiteUrl;

	@Value("${rfi_indicator}")
	private String rfiIndicator;

	@Value("${rfi_indicator_itim}")
	private String rfiIndicatorItim;

	@Value("${isim.user}")
	private String isimUser;

	@Value("${isim.password}")
	private String isimPassword;

	@Value("${service.identitytool.application-url}")
	private String identitytoolApplicationUrl;

	@Value("${encryptor.shared.secret}")
	private String encryptorSharedSecret;

	@Value("${service.ui.search-limit}")
	private int uiSearchLimit;

	@RefreshScope
	@Bean
	public InfoContributor applicationPropertiesContributor() {
		return new ApplicationPropertiesInfoContributor(this);
	}

	@PostConstruct
	public void init() {
		if (StringUtils.isNotEmpty(this.getEnvironment())) {
			System.setProperty("ENVIRONMENT", this.getEnvironment());
		}

		if (StringUtils.isNotEmpty(this.getWerkgeversUrl())) {
			System.setProperty("werkgeversUrl", this.getWerkgeversUrl());
		}

		if (StringUtils.isNotEmpty(this.getInfoHeaderVisible())) {
			System.setProperty("infoheaderVisible", this.getInfoHeaderVisible());
		}

		if (StringUtils.isNotEmpty(this.getHeaderUri())) {
			System.setProperty("headerUri", this.getHeaderUri());
		}

		if (StringUtils.isNotEmpty(this.getFooterUri())) {
			System.setProperty("footerUri", this.getFooterUri());
		}
	}
}
