package com.vortexbird.ordenesPago.entity;

import com.vortexbird.ordenesPago.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad de auditoría para cambios de estado de órdenes.
 * 
 * Esta tabla es alimentada automáticamente por un TRIGGER de base de datos
 * que se dispara cada vez que se actualiza el estado de una orden.
 * 
 * Propósito:
 * - Mantener historial inmutable de cambios de estado
 * - Trazabilidad: quién cambió qué y cuándo
 * - Cumplimiento y auditoría
 * 
 * NO se maneja directamente desde el código, solo consulta.
 */
@Entity
@Table(name = "order_status_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long orderId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus previousStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus newStatus;
    
    @Column(nullable = false)
    private Long changedBy;
    
    @Column(nullable = false)
    private LocalDateTime changedAt;
}
