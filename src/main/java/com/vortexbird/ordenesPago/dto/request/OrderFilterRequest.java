package com.vortexbird.ordenesPago.dto.request;

import com.vortexbird.ordenesPago.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para filtrar órdenes en consultas.
 * Usado en GET /api/orders con parámetros de query
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Filtros para búsqueda de órdenes")
public class OrderFilterRequest {
    
    @Schema(description = "Estado de la orden", example = "PENDING")
    private OrderStatus status;
    
    @Schema(description = "Monto mínimo", example = "100000")
    private BigDecimal minAmount;
    
    @Schema(description = "Monto máximo", example = "5000000")
    private BigDecimal maxAmount;
    
    @Schema(description = "Fecha de creación desde", example = "2026-03-01T00:00:00")
    private LocalDateTime createdFrom;
    
    @Schema(description = "Fecha de creación hasta", example = "2026-03-31T23:59:59")
    private LocalDateTime createdTo;
    
    @Schema(description = "Buscar solo mis órdenes (OPERATOR)", example = "true")
    @Builder.Default
    private Boolean onlyMyOrders = false;
}
