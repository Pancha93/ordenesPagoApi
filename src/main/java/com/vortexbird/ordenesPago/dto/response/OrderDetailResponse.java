package com.vortexbird.ordenesPago.dto.response;

import com.vortexbird.ordenesPago.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para detalle completo de una orden.
 * Usado en GET /api/orders/{id}
 * Incluye toda la información de la orden más la factura si existe.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Detalle completo de una orden de pago")
public class OrderDetailResponse {
    
    @Schema(description = "ID de la orden")
    private Long id;
    
    @Schema(description = "Descripción detallada")
    private String description;
    
    @Schema(description = "Monto")
    private BigDecimal amount;
    
    @Schema(description = "Estado actual")
    private OrderStatus status;
    
    @Schema(description = "Razón de rechazo (si aplica)")
    private String rejectionReason;
    
    @Schema(description = "Email del usuario creador")
    private String createdByEmail;
    
    @Schema(description = "Nombre del usuario creador")
    private String createdByName;
    
    @Schema(description = "Email del aprobador (si aplica)")
    private String approvedByEmail;
    
    @Schema(description = "Nombre del aprobador (si aplica)")
    private String approvedByName;
    
    @Schema(description = "Fecha de aprobación/rechazo")
    private LocalDateTime approvedAt;
    
    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;
    
    @Schema(description = "Fecha de última actualización")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Información de la factura adjunta")
    private InvoiceResponse invoice;
}
