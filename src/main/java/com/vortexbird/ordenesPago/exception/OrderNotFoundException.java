package com.vortexbird.ordenesPago.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando no se encuentra una orden.
 * HTTP Status: 404 NOT FOUND
 */
public class OrderNotFoundException extends BusinessException {
    
    public OrderNotFoundException(Long orderId) {
        super("Orden no encontrada con ID: " + orderId, 
              HttpStatus.NOT_FOUND, 
              "ORDER_NOT_FOUND");
    }
}
