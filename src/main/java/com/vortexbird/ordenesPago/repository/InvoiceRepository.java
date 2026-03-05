package com.vortexbird.ordenesPago.repository;

import com.vortexbird.ordenesPago.entity.Invoice;
import com.vortexbird.ordenesPago.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Invoice.
 * 
 * Funcionalidades:
 * - Buscar factura por orden
 * - Verificar si una orden tiene factura
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    /**
     * Busca una factura por la orden asociada
     */
    Optional<Invoice> findByOrder(Order order);
    
    /**
     * Busca una factura por ID de orden
     */
    Optional<Invoice> findByOrderId(Long orderId);
    
    /**
     * Verifica si una orden tiene factura adjunta
     */
    boolean existsByOrderId(Long orderId);
    
    /**
     * Busca factura por storage key
     */
    Optional<Invoice> findByStorageKey(String storageKey);
}
