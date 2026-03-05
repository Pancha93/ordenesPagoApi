package com.vortexbird.ordenesPago.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de respuesta estándar para errores.
 * Usado por el GlobalExceptionHandler.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta de error estándar")
public class ErrorResponse {
    
    @Schema(description = "Timestamp del error", example = "2026-03-04T10:30:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "Código de estado HTTP", example = "400")
    private Integer status;
    
    @Schema(description = "Descripción del código HTTP", example = "Bad Request")
    private String error;
    
    @Schema(description = "Mensaje descriptivo del error", example = "La orden debe estar en estado PENDING")
    private String message;
    
    @Schema(description = "Código de error de negocio", example = "INVALID_STATE_TRANSITION")
    private String errorCode;
    
    @Schema(description = "Errores de validación por campo")
    private Map<String, String> fieldErrors;
    
    @Schema(description = "Path del endpoint", example = "/api/orders/1/approve")
    private String path;
}
