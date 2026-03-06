package com.vortexbird.ordenesPago.enums;

/**
 * Estados posibles de una Orden de Pago.
 * 
 * Transiciones válidas:
 * - PENDING -> APPROVED (solo ADMIN)
 * - PENDING -> REJECTED (solo ADMIN)
 * 
 * Nota: El archivado se maneja con el campo booleano 'archived',
 * no con un estado adicional.
 */
public enum OrderStatus {
    /**
     * Orden recién creada, pendiente de revisión
     */
    PENDING,
    
    /**
     * Orden aprobada por un administrador
     */
    APPROVED,
    
    /**
     * Orden rechazada por un administrador
     */
    REJECTED
}
