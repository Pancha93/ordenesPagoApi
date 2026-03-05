package com.vortexbird.ordenesPago.controller;

import com.vortexbird.ordenesPago.dto.request.CreateOrderRequest;
import com.vortexbird.ordenesPago.dto.request.OrderFilterRequest;
import com.vortexbird.ordenesPago.dto.request.RejectOrderRequest;
import com.vortexbird.ordenesPago.dto.response.OrderDetailResponse;
import com.vortexbird.ordenesPago.dto.response.OrderResponse;
import com.vortexbird.ordenesPago.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Orders", description = "Gestión de órdenes de pago")
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    @Operation(summary = "Crear orden (OPERATOR)", description = "Permite a un OPERATOR crear una nueva orden de pago")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Principal principal) {
        OrderResponse response = orderService.createOrder(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Listar órdenes", description = "Lista órdenes con filtros y paginación")
    public ResponseEntity<Page<OrderResponse>> listOrders(
            @ModelAttribute OrderFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            Principal principal) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderResponse> response = orderService.listOrders(filter, pageable, principal.getName());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de orden", description = "Obtiene información completa de una orden")
    public ResponseEntity<OrderDetailResponse> getOrderById(
            @PathVariable Long id,
            Principal principal) {
        OrderDetailResponse response = orderService.getOrderById(id, principal.getName());
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/approve")
    @Operation(summary = "Aprobar orden (ADMIN)", description = "Permite a un ADMIN aprobar una orden")
    public ResponseEntity<OrderResponse> approveOrder(
            @PathVariable Long id,
            Principal principal) {
        OrderResponse response = orderService.approveOrder(id, principal.getName());
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/reject")
    @Operation(summary = "Rechazar orden (ADMIN)", description = "Permite a un ADMIN rechazar una orden")
    public ResponseEntity<OrderResponse> rejectOrder(
            @PathVariable Long id,
            @Valid @RequestBody RejectOrderRequest request,
            Principal principal) {
        OrderResponse response = orderService.rejectOrder(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }
}
