package com.vortexbird.ordenesPago.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuración para habilitar auditoría automática de JPA.
 * 
 * Permite que las anotaciones @CreatedDate y @LastModifiedDate
 * en las entidades se actualicen automáticamente.
 * 
 * También soporta @CreatedBy y @LastModifiedBy si se configura AuditorAware.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
