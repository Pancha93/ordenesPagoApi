package com.vortexbird.ordenesPago.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando un usuario intenta realizar una acción no autorizada.
 * Ejemplo: OPERATOR intenta aprobar una orden
 * HTTP Status: 403 FORBIDDEN
 */
public class UnauthorizedActionException extends BusinessException {
    
    public UnauthorizedActionException(String action) {
        super("Acción no autorizada: " + action,
              HttpStatus.FORBIDDEN,
              "UNAUTHORIZED_ACTION");
    }
}
