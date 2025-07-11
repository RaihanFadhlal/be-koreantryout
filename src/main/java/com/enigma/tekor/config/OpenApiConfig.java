package com.enigma.tekor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI openAPI() {
                final String bearerSchemeName = "bearerAuth";

                final String ngrokSchemeName = "ngrok-skip-browser-warning";

                return new OpenAPI()
                                .addServersItem(new Server().url("https://0a44046f54c6.ngrok-free.app")
                                                .description("Production Development Server"))
                                .addServersItem(new Server().url("http://localhost:8081")
                                                .description("Local Development Server"))
                                .addSecurityItem(new SecurityRequirement().addList(bearerSchemeName))
                                .addSecurityItem(new SecurityRequirement().addList(ngrokSchemeName))

                                .components(
                                                new Components()
                                                                .addSecuritySchemes(bearerSchemeName,
                                                                                new SecurityScheme()
                                                                                                .name(bearerSchemeName)
                                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                                .scheme("bearer")
                                                                                                .bearerFormat("JWT"))
                                                                .addSecuritySchemes(ngrokSchemeName,
                                                                                new SecurityScheme()
                                                                                                .name(ngrokSchemeName)
                                                                                                .type(SecurityScheme.Type.APIKEY)
                                                                                                .in(SecurityScheme.In.HEADER)))
                                .info(new Info()
                                                .title("TE-KOR API Documentation")
                                                .description("Korean Language Try out App")
                                                .version("v1.0"));
        }
}