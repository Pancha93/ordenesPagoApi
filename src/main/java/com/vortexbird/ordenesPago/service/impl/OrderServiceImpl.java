package com.vortexbird.ordenesPago.service.impl;

import com.vortexbird.ordenesPago.dto.request.CreateOrderRequest;
import com.vortexbird.ordenesPago.dto.request.OrderFilterRequest;
import com.vortexbird.ordenesPago.dto.request.RejectOrderRequest;
import com.vortexbird.ordenesPago.dto.response.InvoiceResponse;
import com.vortexbird.ordenesPago.dto.response.OrderDetailResponse;
import com.vortexbird.ordenesPago.dto.response.OrderResponse;
import com.vortexbird.ordenesPago.entity.Invoice;
import com.vortexbird.ordenesPago.entity.Order;
import com.vortexbird.ordenesPago.entity.User;
import com.vortexbird.ordenesPago.enums.OrderStatus;
import com.vortexbird.ordenesPago.enums.UserRole;
import com.vortexbird.ordenesPago.exception.InvalidStateTransitionException;
import com.vortexbird.ordenesPago.exception.OrderNotFoundException;
import com.vortexbird.ordenesPago.repository.InvoiceRepository;
import com.vortexbird.ordenesPago.repository.OrderRepository;
import com.vortexbird.ordenesPago.repository.UserRepository;
import com.vortexbird.ordenesPago.service.ExternalNotificationService;
import com.vortexbird.ordenesPago.service.OrderService;
import com.vortexbird.ordenesPago.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementación del servicio de órdenes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final ExternalNotificationService externalNotificationService;
    private final StorageService storageService;
    
    @Override
    @PreAuthorize("hasRole('OPERATOR')")
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String userEmail) {
        log.info("Creating order by user: {}", userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        
        Order order = Order.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .status(OrderStatus.PENDING)
                .createdBy(user)
                .archived(false)
                .build();
        
        order = orderRepository.save(order);
        
        log.info("Order created with ID: {}", order.getId());
        
        return mapToResponse(order);
    }
    
    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Transactional(readOnly = true)
    public Page<OrderResponse> listOrders(OrderFilterRequest filter, Pageable pageable, String userEmail) {
        log.info("Listing orders for user: {}", userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        
        Page<Order> orders;
        
        // OPERATOR solo ve sus propias órdenes, ADMIN ve todas
        if (user.getRole() == UserRole.OPERATOR || Boolean.TRUE.equals(filter.getOnlyMyOrders())) {
            if (filter.getStatus() != null) {
                orders = orderRepository.findByCreatedByAndStatus(user, filter.getStatus(), pageable);
            } else {
                orders = orderRepository.findByCreatedBy(user, pageable);
            }
        } else {
            // ADMIN ve todas
            if (filter.getStatus() != null) {
                orders = orderRepository.findByStatus(filter.getStatus(), pageable);
            } else {
                orders = orderRepository.findAll(pageable);
            }
        }
        
        return orders.map(this::mapToResponse);
    }
    
    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderById(Long id, String userEmail) {
        log.info("Getting order {} for user {}", id, userEmail);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        
        return mapToDetailResponse(order);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OrderResponse approveOrder(Long id, String userEmail) {
        log.info("Approving order {} by admin {}", id, userEmail);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        
        // Validar transición de estado
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidStateTransitionException(
                    "Solo se pueden aprobar órdenes en estado PENDING. Estado actual: " + order.getStatus());
        }
        
        User admin = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        
        // Actualizar orden
        order.setStatus(OrderStatus.APPROVED);
        order.setApprovedBy(admin);
        order.setApprovedAt(LocalDateTime.now());
        
        order = orderRepository.save(order);
        
        log.info("Order {} approved successfully", id);
        
        // Notificar sistema externo (asíncrono, no debe bloquear)
        try {
            externalNotificationService.notifyOrderApproved(order);
        } catch (Exception e) {
            log.error("Failed to notify external system for order {}", id, e);
            // No hacer rollback de la aprobación
        }
        
        return mapToResponse(order);
    }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OrderResponse rejectOrder(Long id, RejectOrderRequest request, String userEmail) {
        log.info("Rejecting order {} by admin {}", id, userEmail);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        
        // Validar transición de estado
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidStateTransitionException(
                    "Solo se pueden rechazar órdenes en estado PENDING. Estado actual: " + order.getStatus());
        }
        
        User admin = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        
        // Actualizar orden
        order.setStatus(OrderStatus.REJECTED);
        order.setApprovedBy(admin);
        order.setApprovedAt(LocalDateTime.now());
        order.setRejectionReason(request.getRejectionReason());
        
        order = orderRepository.save(order);
        
        log.info("Order {} rejected successfully", id);
        
        return mapToResponse(order);
    }
    
    private OrderResponse mapToResponse(Order order) {
        boolean hasInvoice = invoiceRepository.existsByOrderId(order.getId());
        
        return OrderResponse.builder()
                .id(order.getId())
                .description(order.getDescription())
                .amount(order.getAmount())
                .status(order.getStatus())
                .createdByEmail(order.getCreatedBy().getEmail())
                .createdByName(order.getCreatedBy().getFullName())
                .hasInvoice(hasInvoice)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .approvedByEmail(order.getApprovedBy() != null ? order.getApprovedBy().getEmail() : null)
                .approvedAt(order.getApprovedAt())
                .build();
    }
    
    private OrderDetailResponse mapToDetailResponse(Order order) {
        InvoiceResponse invoiceResponse = null;
        
        Invoice invoice = invoiceRepository.findByOrderId(order.getId()).orElse(null);
        if (invoice != null) {
            // Regenerar URL de descarga
            String downloadUrl = storageService.generateDownloadUrl(invoice.getStorageKey(), 15);
            
            invoiceResponse = InvoiceResponse.builder()
                    .id(invoice.getId())
                    .orderId(order.getId())
                    .originalFileName(invoice.getOriginalFileName())
                    .contentType(invoice.getContentType())
                    .fileSizeBytes(invoice.getFileSizeBytes())
                    .downloadUrl(downloadUrl)
                    .uploadedByEmail(invoice.getUploadedBy().getEmail())
                    .uploadedAt(invoice.getUploadedAt())
                    .build();
        }
        
        return OrderDetailResponse.builder()
                .id(order.getId())
                .description(order.getDescription())
                .amount(order.getAmount())
                .status(order.getStatus())
                .rejectionReason(order.getRejectionReason())
                .createdByEmail(order.getCreatedBy().getEmail())
                .createdByName(order.getCreatedBy().getFullName())
                .approvedByEmail(order.getApprovedBy() != null ? order.getApprovedBy().getEmail() : null)
                .approvedByName(order.getApprovedBy() != null ? order.getApprovedBy().getFullName() : null)
                .approvedAt(order.getApprovedAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .invoice(invoiceResponse)
                .build();
    }
}
