package com.vortexbird.ordenesPago.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando no se encuentra una factura.
 * HTTP Status: 404 NOT FOUND
 */
public class InvoiceNotFoundException extends BusinessException {
    
    public InvoiceNotFoundException(Long orderId) {
        super("No se encontró factura para la orden ID: " + orderId,
              HttpStatus.NOT_FOUND,
              "INVOICE_NOT_FOUND");
    }
    
    public InvoiceNotFoundException(String message) {
        super(message,
              HttpStatus.NOT_FOUND,
              "INVOICE_NOT_FOUND");
    }
}
