package org.bool.k8ron.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
            .info(new Info().title("k8ron API")
                .description("Cron for k8s")
                .version(buildProperties != null ? buildProperties.getVersion() : "dev")
                .license(new License().name("MIT License")));
    }
}
