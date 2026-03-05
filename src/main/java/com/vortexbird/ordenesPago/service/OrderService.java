package com.vortexbird.ordenesPago.service;

import com.vortexbird.ordenesPago.dto.request.CreateOrderRequest;
import com.vortexbird.ordenesPago.dto.request.OrderFilterRequest;
import com.vortexbird.ordenesPago.dto.request.RejectOrderRequest;
import com.vortexbird.ordenesPago.dto.response.OrderDetailResponse;
import com.vortexbird.ordenesPago.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request, String userEmail);
    Page<OrderResponse> listOrders(OrderFilterRequest filter, Pageable pageable, String userEmail);
    OrderDetailResponse getOrderById(Long id, String userEmail);
    OrderResponse approveOrder(Long id, String userEmail);
    OrderResponse rejectOrder(Long id, RejectOrderRequest request, String userEmail);
}
