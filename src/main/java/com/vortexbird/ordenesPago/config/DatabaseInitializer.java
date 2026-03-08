package com.vortexbird.ordenesPago.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Componente que ejecuta scripts SQL después de que la aplicación esté lista.
 * Útil para crear triggers y procedimientos almacenados que dependen de las tablas
 * creadas por Hibernate/JPA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Se ejecuta cuando la aplicación está completamente iniciada
     * (después de que Hibernate haya creado todas las tablas).
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDatabase() {
        log.info("=================================================");
        log.info("Inicializando triggers y procedimientos almacenados");
        log.info("=================================================");
        
        try {
            // Ejecutar trigger de auditoría
            executeSqlScript("db/order_status_trigger.sql");
            log.info("✓ Trigger de auditoría creado correctamente");
            
            // Ejecutar procedimiento almacenado
            executeSqlScript("db/archive_rejected_orders.sql");
            log.info("✓ Procedimiento almacenado creado correctamente");
            
            log.info("=================================================");
            log.info("Base de datos inicializada correctamente");
            log.info("=================================================");
            
        } catch (Exception e) {
            log.error("Error al inicializar base de datos", e);
            // No lanzar excepción para que la aplicación siga funcionando
            // Los triggers y procedimientos son opcionales
        }
    }
    
    /**
     * Lee y ejecuta un script SQL desde el classpath
     */
    private void executeSqlScript(String scriptPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(scriptPath);
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            String sql = reader.lines().collect(Collectors.joining("\n"));
            
            // Ejecutar el script completo
            jdbcTemplate.execute(sql);
            
            log.debug("Script ejecutado: {}", scriptPath);
        }
    }
}
