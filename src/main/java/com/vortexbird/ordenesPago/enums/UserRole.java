package com.vortexbird.ordenesPago.enums;

/**
 * Roles de usuario en el sistema (RBAC).
 * 
 * OPERATOR:
 * - Crear órdenes
 * - Subir facturas
 * - Ver sus propias órdenes
 * 
 * ADMIN:
 * - Ver todas las órdenes
 * - Aprobar/rechazar órdenes
 * - Ver y descargar facturas
 */
public enum UserRole {
    /**
     * Operador: Puede crear órdenes y subir facturas
     */
    OPERATOR,
    
    /**
     * Administrador: Puede aprobar/rechazar órdenes y ver todo
     */
    ADMIN
}
