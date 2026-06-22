package com.hospital.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Swagger UI (/swagger-ui.html) with a global "Bearer token" auth box,
 * so you can paste a JWT once and try every protected endpoint from the browser.
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearerAuth";

    @Bean
    public OpenAPI hospitalOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Multispeciality Hospital API")
                        .version("1.0.0")
                        .description("Role-based hospital management API — Hyderabad, Telangana, India "
                                + "(admin / doctor / receptionist)."))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
