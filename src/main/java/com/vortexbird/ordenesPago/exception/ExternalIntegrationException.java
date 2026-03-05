package com.vortexbird.ordenesPago.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando falla la integración con sistema externo.
 * Esta excepción NO debe causar rollback de transacciones.
 * HTTP Status: 502 BAD GATEWAY
 */
public class ExternalIntegrationException extends BusinessException {
    
    public ExternalIntegrationException(String message) {
        super("Error en integración externa: " + message,
              HttpStatus.BAD_GATEWAY,
              "EXTERNAL_INTEGRATION_ERROR");
    }
    
    public ExternalIntegrationException(String message, Throwable cause) {
        super("Error en integración externa: " + message,
              HttpStatus.BAD_GATEWAY,
              "EXTERNAL_INTEGRATION_ERROR",
              cause);
    }
}
