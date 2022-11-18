package org.home.productivity.traverse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Rest configuration
 * <p>
 * Swagger/OpenAPI config - sample:
 * https://github.com/hmcts/spring-boot-template/blob/master/src/main/java/uk/gov/hmcts/reform/demo/config/OpenAPIConfiguration.java
 */
@Configuration
public class RestConfig {

    @Bean
    public OpenAPI openAPI() {
        // @formatter:off
        return new OpenAPI()
            .info(
                new Info().title("Koat API")
                          .description("Koat API")
                          .version("v0.1")
                          .license(new License().name("MIT").url("https://opensource.org/licenses/MIT"))
            );
        // @formatter:on
    }
}