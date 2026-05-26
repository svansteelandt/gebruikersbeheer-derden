package be.vdab.gebruikersbeheer.derden.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.repository.init.Jackson2RepositoryPopulatorFactoryBean;

import java.io.IOException;

@Configuration
public class LocalDataConfig {

    @Autowired
    private ObjectMapper objectMapper;
    
    @Bean
    @Profile("local")
    public Jackson2RepositoryPopulatorFactoryBean repositoryPopulator() throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        Resource[] resources = resolver.getResources("classpath*:local-data/*.json");
        Jackson2RepositoryPopulatorFactoryBean factory = new Jackson2RepositoryPopulatorFactoryBean();
        factory.setMapper(objectMapper);
        factory.setResources(resources);
        return factory;
    }
}
