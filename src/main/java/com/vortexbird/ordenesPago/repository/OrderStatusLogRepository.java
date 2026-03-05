package com.vortexbird.ordenesPago.repository;

import com.vortexbird.ordenesPago.entity.OrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad OrderStatusLog.
 * 
 * Esta tabla es de solo lectura desde el código Java.
 * Los registros son insertados automáticamente por un trigger SQL.
 * 
 * Usado para:
 * - Consultar historial de cambios de estado
 * - Auditoría y reportes
 */
@Repository
public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, Long> {
    
    /**
     * Obtiene el historial de cambios de estado de una orden específica
     * Ordenado por fecha (más reciente primero)
     */
    List<OrderStatusLog> findByOrderIdOrderByChangedAtDesc(Long orderId);
    
    /**
     * Obtiene todos los cambios realizados por un usuario
     */
    List<OrderStatusLog> findByChangedBy(Long userId);
}
