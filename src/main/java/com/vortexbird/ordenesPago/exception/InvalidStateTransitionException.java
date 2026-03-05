package com.vortexbird.ordenesPago.exception;

import com.vortexbird.ordenesPago.enums.OrderStatus;
import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando se intenta una transición de estado inválida.
 * Ejemplo: Intentar aprobar una orden que ya está APPROVED o REJECTED
 * HTTP Status: 400 BAD REQUEST
 */
public class InvalidStateTransitionException extends BusinessException {
    
    public InvalidStateTransitionException(OrderStatus from, OrderStatus to) {
        super(String.format("Transición de estado inválida: de %s a %s", from, to),
              HttpStatus.BAD_REQUEST,
              "INVALID_STATE_TRANSITION");
    }
    
    public InvalidStateTransitionException(String message) {
        super(message,
              HttpStatus.BAD_REQUEST,
              "INVALID_STATE_TRANSITION");
    }
}
