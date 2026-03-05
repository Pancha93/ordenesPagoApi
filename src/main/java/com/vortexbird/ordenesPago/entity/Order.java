package com.vortexbird.ordenesPago.entity;

import com.vortexbird.ordenesPago.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una Orden de Pago.
 * 
 * Ciclo de vida:
 * 1. OPERATOR crea la orden (estado: PENDING)
 * 2. OPERATOR sube factura (opcional al crear)
 * 3. ADMIN aprueba (APPROVED) o rechaza (REJECTED)
 * 4. Si se aprueba, se notifica al sistema externo
 * 5. Si se rechaza, puede archivarse luego (ARCHIVED)
 * 
 * Relaciones:
 * - ManyToOne con User (createdBy)
 * - ManyToOne con User (approvedBy)
 * - OneToOne con Invoice
 * 
 * Auditoría:
 * - Cada cambio de estado se registra automáticamente en OrderStatusLog (trigger SQL)
 */
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    private String description;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    @Column
    private LocalDateTime approvedAt;
    
    @Column(length = 500)
    private String rejectionReason;
    
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Invoice invoice;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean archived = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
