package com.vortexbird.ordenesPago.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para verificar el estado de salud de la aplicación.
 * No requiere autenticación.
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Endpoints para verificar el estado del servicio")
public class HealthController {

    @GetMapping
    @Operation(
            summary = "Verificar estado del servicio",
            description = "Retorna información básica sobre el estado de la aplicación. No requiere autenticación."
    )
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Órdenes de Pago API");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/ping")
    @Operation(
            summary = "Ping simple",
            description = "Responde con 'pong' para verificar conectividad básica"
    )
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
