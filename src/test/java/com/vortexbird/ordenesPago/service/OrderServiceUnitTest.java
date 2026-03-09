package com.vortexbird.ordenesPago.service;

import com.vortexbird.ordenesPago.dto.request.CreateOrderRequest;
import com.vortexbird.ordenesPago.dto.request.RejectOrderRequest;
import com.vortexbird.ordenesPago.dto.response.OrderResponse;
import com.vortexbird.ordenesPago.entity.Order;
import com.vortexbird.ordenesPago.entity.User;
import com.vortexbird.ordenesPago.enums.OrderStatus;
import com.vortexbird.ordenesPago.enums.UserRole;
import com.vortexbird.ordenesPago.exception.InvalidStateTransitionException;
import com.vortexbird.ordenesPago.exception.OrderNotFoundException;
import com.vortexbird.ordenesPago.repository.InvoiceRepository;
import com.vortexbird.ordenesPago.repository.OrderRepository;
import com.vortexbird.ordenesPago.repository.UserRepository;
import com.vortexbird.ordenesPago.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test unitario con mocks para OrderService.
 * Prueba la lógica de negocio de forma aislada.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service - Tests Unitarios con Mocks")
class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ExternalNotificationService externalNotificationService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User operator;
    private User admin;
    private Order pendingOrder;

    @BeforeEach
    void setUp() {
        operator = User.builder()
                .id(1L)
                .email("operator@test.com")
                .fullName("Test Operator")
                .role(UserRole.OPERATOR)
                .build();

        admin = User.builder()
                .id(2L)
                .email("admin@test.com")
                .fullName("Test Admin")
                .role(UserRole.ADMIN)
                .build();

        pendingOrder = Order.builder()
                .id(100L)
                .description("Test Order")
                .amount(new BigDecimal("1500000.00"))
                .status(OrderStatus.PENDING)
                .createdBy(operator)
                .archived(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Crear orden - Success")
    void testCreateOrderSuccess() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setDescription("Nueva orden de pago");
        request.setAmount(new BigDecimal("2000000"));

        when(userRepository.findByEmail(operator.getEmail())).thenReturn(Optional.of(operator));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(101L);
            return order;
        });
        when(invoiceRepository.existsByOrderId(anyLong())).thenReturn(false);

        // Act
        OrderResponse response = orderService.createOrder(request, operator.getEmail());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getDescription()).isEqualTo("Nueva orden de pago");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("2000000"));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getCreatedByEmail()).isEqualTo(operator.getEmail());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(userRepository, times(1)).findByEmail(operator.getEmail());
    }

    @Test
    @DisplayName("Aprobar orden - Success")
    void testApproveOrderSuccess() {
        // Arrange
        when(orderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.existsByOrderId(anyLong())).thenReturn(false);
        doNothing().when(externalNotificationService).notifyOrderApproved(any(Order.class));

        // Act
        OrderResponse response = orderService.approveOrder(100L, admin.getEmail());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.APPROVED);
        assertThat(response.getApprovedByEmail()).isEqualTo(admin.getEmail());
        assertThat(response.getApprovedAt()).isNotNull();

        verify(orderRepository, times(1)).findById(100L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(externalNotificationService, times(1)).notifyOrderApproved(any(Order.class));
    }

    @Test
    @DisplayName("Aprobar orden - Orden no encontrada")
    void testApproveOrderNotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.approveOrder(999L, admin.getEmail()))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
        verify(externalNotificationService, never()).notifyOrderApproved(any(Order.class));
    }

    @Test
    @DisplayName("Aprobar orden - Estado inválido (ya aprobada)")
    void testApproveOrderInvalidState() {
        // Arrange
        Order alreadyApprovedOrder = Order.builder()
                .id(100L)
                .description("Already Approved")
                .amount(new BigDecimal("1000000"))
                .status(OrderStatus.APPROVED)
                .createdBy(operator)
                .approvedBy(admin)
                .approvedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(100L)).thenReturn(Optional.of(alreadyApprovedOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.approveOrder(100L, admin.getEmail()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Solo se pueden aprobar órdenes en estado PENDING");

        verify(orderRepository, times(1)).findById(100L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Rechazar orden - Success")
    void testRejectOrderSuccess() {
        // Arrange
        RejectOrderRequest rejectRequest = new RejectOrderRequest();
        rejectRequest.setRejectionReason("Documentación incompleta");

        when(orderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceRepository.existsByOrderId(anyLong())).thenReturn(false);

        // Act
        OrderResponse response = orderService.rejectOrder(100L, rejectRequest, admin.getEmail());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(response.getApprovedByEmail()).isEqualTo(admin.getEmail());
        assertThat(response.getApprovedAt()).isNotNull();

        verify(orderRepository, times(1)).findById(100L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(externalNotificationService, never()).notifyOrderApproved(any(Order.class));
    }

    @Test
    @DisplayName("Rechazar orden - Estado inválido (ya rechazada)")
    void testRejectOrderInvalidState() {
        // Arrange
        Order alreadyRejectedOrder = Order.builder()
                .id(100L)
                .description("Already Rejected")
                .amount(new BigDecimal("1000000"))
                .status(OrderStatus.REJECTED)
                .createdBy(operator)
                .approvedBy(admin)
                .rejectionReason("Previous reason")
                .build();

        RejectOrderRequest rejectRequest = new RejectOrderRequest();
        rejectRequest.setRejectionReason("Nueva razón");

        when(orderRepository.findById(100L)).thenReturn(Optional.of(alreadyRejectedOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.rejectOrder(100L, rejectRequest, admin.getEmail()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Solo se pueden rechazar órdenes en estado PENDING");

        verify(orderRepository, times(1)).findById(100L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Obtener orden por ID - Success")
    void testGetOrderByIdSuccess() {
        // Arrange
        when(orderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));

        // Act
        var response = orderService.getOrderById(100L, operator.getEmail());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getDescription()).isEqualTo("Test Order");

        verify(orderRepository, times(1)).findById(100L);
    }

    @Test
    @DisplayName("Obtener orden por ID - No encontrada")
    void testGetOrderByIdNotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderById(999L, operator.getEmail()))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, times(1)).findById(999L);
    }
}
