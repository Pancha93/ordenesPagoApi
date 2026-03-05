package com.vortexbird.ordenesPago.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad que representa una factura adjunta a una orden.
 * 
 * Almacenamiento:
 * - El archivo físico se almacena en S3/Blob Storage
 * - Esta entidad solo guarda metadata y referencia (storageKey)
 * - downloadUrl es temporal (presigned URL)
 * 
 * Relaciones:
 * - OneToOne con Order
 * - ManyToOne con User (uploadedBy)
 * 
 * Tipos de archivo soportados:
 * - PDF (application/pdf)
 * - Imágenes (image/jpeg, image/png)
 */
@Entity
@Table(name = "invoices")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;
    
    @Column(nullable = false, length = 255)
    private String originalFileName;
    
    @Column(nullable = false, length = 500)
    private String storageKey;
    
    @Column(nullable = false, length = 100)
    private String contentType;
    
    @Column(nullable = false)
    private Long fileSizeBytes;
    
    @Column(length = 1000)
    private String downloadUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}
