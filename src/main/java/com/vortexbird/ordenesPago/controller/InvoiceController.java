package com.vortexbird.ordenesPago.controller;

import com.vortexbird.ordenesPago.dto.response.InvoiceResponse;
import com.vortexbird.ordenesPago.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Invoices", description = "Gestión de facturas")
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    @PostMapping(value = "/upload/{orderId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir factura", description = "Permite subir una factura (PDF o imagen) para una orden")
    public ResponseEntity<InvoiceResponse> uploadInvoice(
            @PathVariable Long orderId,
            @RequestPart("file") MultipartFile file,
            Principal principal) {
        InvoiceResponse response = invoiceService.uploadInvoice(orderId, file, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Obtener factura de orden", description = "Obtiene información de la factura asociada a una orden")
    public ResponseEntity<InvoiceResponse> getInvoiceByOrderId(
            @PathVariable Long orderId,
            Principal principal) {
        InvoiceResponse response = invoiceService.getInvoiceByOrderId(orderId, principal.getName());
        return ResponseEntity.ok(response);
    }
}
