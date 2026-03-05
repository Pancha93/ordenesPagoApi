package com.vortexbird.ordenesPago.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para información de factura.
 * Incluye URL temporal para descarga.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Información de factura adjunta")
public class InvoiceResponse {
    
    @Schema(description = "ID de la factura")
    private Long id;
    
    @Schema(description = "ID de la orden asociada")
    private Long orderId;
    
    @Schema(description = "Nombre original del archivo")
    private String originalFileName;
    
    @Schema(description = "Tipo de contenido")
    private String contentType;
    
    @Schema(description = "Tamaño del archivo en bytes")
    private Long fileSizeBytes;
    
    @Schema(description = "URL temporal de descarga (expira en 15 minutos)")
    private String downloadUrl;
    
    @Schema(description = "Email del usuario que subió la factura")
    private String uploadedByEmail;
    
    @Schema(description = "Fecha de carga")
    private LocalDateTime uploadedAt;
}
