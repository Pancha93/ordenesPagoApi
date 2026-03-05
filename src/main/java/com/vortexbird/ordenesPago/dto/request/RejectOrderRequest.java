package com.vortexbird.ordenesPago.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para rechazar una orden.
 * Entrada del endpoint PATCH /api/orders/{id}/reject
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para rechazar una orden")
public class RejectOrderRequest {
    
    @NotBlank(message = "La razón de rechazo es obligatoria")
    @Size(min = 10, max = 500, message = "La razón debe tener entre 10 y 500 caracteres")
    @Schema(description = "Motivo del rechazo", example = "Factura incompleta o ilegible")
    private String rejectionReason;
}
