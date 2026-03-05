package com.vortexbird.ordenesPago.service.impl;

import com.vortexbird.ordenesPago.dto.response.InvoiceResponse;
import com.vortexbird.ordenesPago.entity.Invoice;
import com.vortexbird.ordenesPago.entity.Order;
import com.vortexbird.ordenesPago.entity.User;
import com.vortexbird.ordenesPago.exception.InvoiceNotFoundException;
import com.vortexbird.ordenesPago.exception.OrderNotFoundException;
import com.vortexbird.ordenesPago.repository.InvoiceRepository;
import com.vortexbird.ordenesPago.repository.OrderRepository;
import com.vortexbird.ordenesPago.repository.UserRepository;
import com.vortexbird.ordenesPago.service.InvoiceService;
import com.vortexbird.ordenesPago.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * Implementación del servicio de facturas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );
    
    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Transactional
    public InvoiceResponse uploadInvoice(Long orderId, MultipartFile file, String userEmail) {
        log.info("Uploading invoice for order {} by user {}", orderId, userEmail);
        
        // Validar tipo de archivo
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Solo PDF o imágenes (JPG, PNG)");
        }
        
        // Buscar orden
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // Buscar usuario
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        
        // Verificar si ya existe factura
        invoiceRepository.findByOrderId(orderId).ifPresent(existing -> {
            // Eliminar archivo anterior
            storageService.delete(existing.getStorageKey());
            invoiceRepository.delete(existing);
        });
        
        // Almacenar archivo
        String storageKey = storageService.store(file, "invoices");
        String downloadUrl = storageService.generateDownloadUrl(storageKey, 15);
        
        // Crear entidad Invoice
        Invoice invoice = Invoice.builder()
                .order(order)
                .originalFileName(file.getOriginalFilename())
                .storageKey(storageKey)
                .contentType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .downloadUrl(downloadUrl)
                .uploadedBy(user)
                .build();
        
        invoice = invoiceRepository.save(invoice);
        
        log.info("Invoice uploaded successfully with ID: {}", invoice.getId());
        
        return mapToResponse(invoice);
    }
    
    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByOrderId(Long orderId, String userEmail) {
        log.info("Getting invoice for order {} by user {}", orderId, userEmail);
        
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new InvoiceNotFoundException(orderId));
        
        // Regenerar URL de descarga (presigned URL temporal)
        String downloadUrl = storageService.generateDownloadUrl(invoice.getStorageKey(), 15);
        invoice.setDownloadUrl(downloadUrl);
        
        return mapToResponse(invoice);
    }
    
    private InvoiceResponse mapToResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .orderId(invoice.getOrder().getId())
                .originalFileName(invoice.getOriginalFileName())
                .contentType(invoice.getContentType())
                .fileSizeBytes(invoice.getFileSizeBytes())
                .downloadUrl(invoice.getDownloadUrl())
                .uploadedByEmail(invoice.getUploadedBy().getEmail())
                .uploadedAt(invoice.getUploadedAt())
                .build();
    }
}
