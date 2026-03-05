package com.vortexbird.ordenesPago.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI 3 (Swagger) para documentación interactiva de la API.
 * 
 * Configura:
 * - Información general del proyecto
 * - Esquema de seguridad JWT Bearer
 * - Servidores disponibles
 * 
 * Acceso: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Órdenes de Pago API")
                        .version("1.0.0")
                        .description("""
                                Sistema de gestión de Órdenes de Pago con las siguientes funcionalidades:
                                - Autenticación basada en JWT
                                - Creación y gestión de órdenes de pago
                                - Carga de facturas (PDF/imágenes)
                                - Aprobación/Rechazo de órdenes (solo ADMIN)
                                - Notificación a sistemas externos
                                - Auditoría de cambios de estado
                                
                                **Roles:**
                                - **ADMIN**: Aprobar/rechazar órdenes, ver todas las órdenes
                                - **OPERATOR**: Crear órdenes, subir facturas
                                """)
                        .contact(new Contact()
                                .name("Vortexbird")
                                .email("soporte@vortexbird.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Desarrollo Local"),
                        new Server()
                                .url("https://api-ordenes-pago.vortexbird.com")
                                .description("Producción")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingrese el token JWT obtenido del endpoint /api/auth/login")
                        )
                );
    }
}
