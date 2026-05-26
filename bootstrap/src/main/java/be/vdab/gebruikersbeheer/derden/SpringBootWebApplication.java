package be.vdab.gebruikersbeheer.derden;

import be.vdab.gebruikersbeheer.derden.util.VDABMessageResource;
import be.vdab.hermes.logstash.core.ApplicationId;
import ch.qos.logback.classic.LoggerContext;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.DispatcherType;
import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.util.EnumSet;

@SpringBootApplication
public class SpringBootWebApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebApplication.class, args);
	}

	public SpringBootWebApplication() {
		ApplicationId.setCurrentApplication("gebruikersbeheer-derden");
	}

	@PreDestroy
	public void stopLoggerContext() {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		lc.stop();
	}

	@Bean
	public MessageSource messageSource(Environment environment) {
		VDABMessageResource messageSource = new VDABMessageResource(environment);

		messageSource.setBasename("classpath:messages");
		messageSource.setDefaultEncoding("UTF-8");

		return messageSource;
	}

	@Bean
	public ViewResolver getViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/view");
		resolver.setSuffix(".jsp");
		resolver.setViewClass(JstlView.class);
		resolver.setRedirectHttp10Compatible(false);

		return resolver;
	}

	@Bean
	public FilterRegistrationBean<ConfigurableSiteMeshFilter> getSiteMeshFilter() {
		ConfigurableSiteMeshFilter siteMeshFilter = new ConfigurableSiteMeshFilter();

		FilterRegistrationBean<ConfigurableSiteMeshFilter> bean = new FilterRegistrationBean<>();
		bean.setFilter(siteMeshFilter);
		bean.setName("siteMeshFilter");
		bean.addUrlPatterns("*.jsp");
		bean.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));

		return bean;
	}
}

