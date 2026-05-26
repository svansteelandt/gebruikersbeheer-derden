package be.vdab.gebruikersbeheer.derden.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        Info info = new Info()
                .title("GEBRUIKERSBEHEER-DERDEN API")
                .license(new License().name("VDAB License"))
                .version("1.0.0");

        return openApi -> openApi.info(info);
    }
}
