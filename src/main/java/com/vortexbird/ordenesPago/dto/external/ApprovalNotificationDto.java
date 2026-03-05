package com.vortexbird.ordenesPago.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para notificación al sistema externo cuando una orden es aprobada.
 * Enviado a: POST {external.api.base-url}{external.api.notification-endpoint}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload de notificación externa para orden aprobada")
public class ApprovalNotificationDto {
    
    @Schema(description = "ID de la orden aprobada", example = "123")
    private Long orderId;
    
    @Schema(description = "Estado de la orden", example = "APPROVED")
    private String status;
    
    @Schema(description = "Descripción de la orden", example = "Pago de servicios")
    private String description;
    
    @Schema(description = "Monto de la orden", example = "1500000.00")
    private BigDecimal amount;
    
    @Schema(description = "Fecha de aprobación", example = "2026-03-04T11:15:00")
    private LocalDateTime approvedAt;
    
    @Schema(description = "Email del usuario que aprobó", example = "admin@vortexbird.com")
    private String approvedBy;
}
