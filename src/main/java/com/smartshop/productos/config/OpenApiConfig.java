package com.smartshop.productos.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Esta anotación registra un esquema de seguridad tipo HTTP Bearer Token
@SecurityScheme(
        name = "BearerAuth",               // Nombre usado en los endpoints
        type = SecuritySchemeType.HTTP,   // Tipo de autenticación
        scheme = "bearer",                // Tipo Bearer
        bearerFormat = "JWT"              // Formato JWT
)
@Configuration
public class OpenApiConfig {

    // Bean que define la configuración de la API (nombre, descripción, etc.)
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartShop - Microservicio de Productos")
                        .version("1.0")
                        .description("API para gestión de productos y recomendaciones inteligentes"));
    }
}
