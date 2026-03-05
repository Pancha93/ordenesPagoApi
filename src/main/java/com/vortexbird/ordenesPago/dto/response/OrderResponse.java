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
 * DTO de respuesta para orden de pago (vista resumida).
 * Usado en listados de órdenes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Información resumida de una orden de pago")
public class OrderResponse {
    
    @Schema(description = "ID de la orden", example = "1")
    private Long id;
    
    @Schema(description = "Descripción de la orden", example = "Pago de servicios")
    private String description;
    
    @Schema(description = "Monto de la orden", example = "1500000.00")
    private BigDecimal amount;
    
    @Schema(description = "Estado actual", example = "PENDING")
    private OrderStatus status;
    
    @Schema(description = "Email del usuario creador", example = "operator@vortexbird.com")
    private String createdByEmail;
    
    @Schema(description = "Nombre del usuario creador", example = "María García")
    private String createdByName;
    
    @Schema(description = "Indica si tiene factura adjunta", example = "true")
    private Boolean hasInvoice;
    
    @Schema(description = "Fecha de creación", example = "2026-03-04T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Fecha de última actualización", example = "2026-03-04T11:15:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Email del aprobador (si aplica)", example = "admin@vortexbird.com")
    private String approvedByEmail;
    
    @Schema(description = "Fecha de aprobación/rechazo", example = "2026-03-04T11:15:00")
    private LocalDateTime approvedAt;
}
