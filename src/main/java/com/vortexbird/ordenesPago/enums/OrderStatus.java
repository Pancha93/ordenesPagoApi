package com.vortexbird.ordenesPago.enums;

/**
 * Estados posibles de una Orden de Pago.
 * 
 * Transiciones válidas:
 * - PENDING -> APPROVED (solo ADMIN)
 * - PENDING -> REJECTED (solo ADMIN)
 * - REJECTED -> ARCHIVED (proceso automático)
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
    REJECTED,
    
    /**
     * Orden archivada por proceso de limpieza (después de REJECTED)
     */
    ARCHIVED
}
