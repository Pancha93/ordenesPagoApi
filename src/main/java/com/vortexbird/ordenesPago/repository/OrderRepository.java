package com.vortexbird.ordenesPago.repository;

import com.vortexbird.ordenesPago.entity.Order;
import com.vortexbird.ordenesPago.entity.User;
import com.vortexbird.ordenesPago.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad Order.
 * 
 * Funcionalidades:
 * - CRUD básico con paginación
 * - Filtrado por estado, creator, fechas
 * - Consultas para OPERATOR (solo sus órdenes)
 * - Consultas para ADMIN (todas las órdenes)
 * - Consultas para proceso de archivado
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    
    /**
     * Busca órdenes creadas por un usuario específico
     * Usado por OPERATOR para ver solo sus órdenes
     */
    Page<Order> findByCreatedBy(User createdBy, Pageable pageable);
    
    /**
     * Busca órdenes por estado
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    /**
     * Busca órdenes creadas por un usuario con un estado específico
     */
    Page<Order> findByCreatedByAndStatus(User createdBy, OrderStatus status, Pageable pageable);
    
    /**
     * Busca órdenes rechazadas antes de una fecha específica (para archivado)
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.updatedAt < :beforeDate AND o.archived = false")
    List<Order> findRejectedBeforeDate(@Param("status") OrderStatus status, @Param("beforeDate") LocalDateTime beforeDate);
    
    /**
     * Cuenta órdenes por estado
     */
    long countByStatus(OrderStatus status);
    
    /**
     * Cuenta órdenes creadas por un usuario
     */
    long countByCreatedBy(User createdBy);
    
    /**
     * Busca órdenes aprobadas en un rango de fechas
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'APPROVED' AND o.approvedAt BETWEEN :startDate AND :endDate")
    List<Order> findApprovedBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
