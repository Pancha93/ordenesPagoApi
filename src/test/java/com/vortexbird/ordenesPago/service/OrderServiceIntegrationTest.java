package com.vortexbird.ordenesPago.service;

import com.vortexbird.ordenesPago.dto.request.CreateOrderRequest;
import com.vortexbird.ordenesPago.dto.request.RejectOrderRequest;
import com.vortexbird.ordenesPago.dto.response.OrderDetailResponse;
import com.vortexbird.ordenesPago.dto.response.OrderResponse;
import com.vortexbird.ordenesPago.entity.User;
import com.vortexbird.ordenesPago.enums.OrderStatus;
import com.vortexbird.ordenesPago.enums.UserRole;
import com.vortexbird.ordenesPago.exception.InvalidStateTransitionException;
import com.vortexbird.ordenesPago.exception.OrderNotFoundException;
import com.vortexbird.ordenesPago.repository.OrderRepository;
import com.vortexbird.ordenesPago.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integración que prueba el flujo completo del negocio:
 * 1. Crear orden como OPERATOR
 * 2. Aprobar orden como ADMIN
 * 3. Intentar aprobar orden ya aprobada (debe fallar)
 * 4. Crear otra orden y rechazarla
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("Order Service - Flujo Completo de Negocio")
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User operator;
    private User admin;

    @BeforeEach
    void setUp() {
        // Limpiar datos previos
        orderRepository.deleteAll();
        userRepository.deleteAll();

        // Crear usuarios de prueba
        operator = User.builder()
                .email("operator-test@vortexbird.com")
                .password(passwordEncoder.encode("test123"))
                .fullName("Test Operator")
                .role(UserRole.OPERATOR)
                .build();
        operator = userRepository.save(operator);

        admin = User.builder()
                .email("admin-test@vortexbird.com")
                .password(passwordEncoder.encode("test123"))
                .fullName("Test Admin")
                .role(UserRole.ADMIN)
                .build();
        admin = userRepository.save(admin);
    }

    @Test
    @WithMockUser(username = "operator-test@vortexbird.com", roles = {"OPERATOR", "ADMIN"})
    @DisplayName("Flujo completo: Crear orden → Aprobar → Verificar estado")
    void testCompleteApprovalFlow() {
        // PASO 1: OPERATOR crea una orden
        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setDescription("Pago de servicios de consultoría Q1 2026");
        createRequest.setAmount(new BigDecimal("2500000.00"));

        OrderResponse createdOrder = orderService.createOrder(createRequest, operator.getEmail());

        // Verificar que la orden se creó correctamente
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getId()).isNotNull();
        assertThat(createdOrder.getDescription()).isEqualTo("Pago de servicios de consultoría Q1 2026");
        assertThat(createdOrder.getAmount()).isEqualByComparingTo(new BigDecimal("2500000.00"));
        assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(createdOrder.getCreatedByEmail()).isEqualTo(operator.getEmail());
        assertThat(createdOrder.getApprovedByEmail()).isNull();
        assertThat(createdOrder.getApprovedAt()).isNull();

        Long orderId = createdOrder.getId();

        // PASO 2: ADMIN aprueba la orden
        OrderResponse approvedOrder = orderService.approveOrder(orderId, admin.getEmail());

        // Verificar que la orden fue aprobada correctamente
        assertThat(approvedOrder).isNotNull();
        assertThat(approvedOrder.getId()).isEqualTo(orderId);
        assertThat(approvedOrder.getStatus()).isEqualTo(OrderStatus.APPROVED);
        assertThat(approvedOrder.getApprovedByEmail()).isEqualTo(admin.getEmail());
        assertThat(approvedOrder.getApprovedAt()).isNotNull();
        assertThat(approvedOrder.getDescription()).isEqualTo("Pago de servicios de consultoría Q1 2026");
        assertThat(approvedOrder.getAmount()).isEqualByComparingTo(new BigDecimal("2500000.00"));

        // PASO 3: Intentar aprobar la orden nuevamente (debe fallar)
        assertThatThrownBy(() -> orderService.approveOrder(orderId, admin.getEmail()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Solo se pueden aprobar órdenes en estado PENDING");
    }

    @WithMockUser(username = "operator-test@vortexbird.com", roles = {"OPERATOR", "ADMIN"})
    @Test
    @DisplayName("Flujo completo: Crear orden → Rechazar → Verificar estado")
    void testCompleteRejectionFlow() {
        // PASO 1: OPERATOR crea una orden
        CreateOrderRequest createRequest = new CreateOrderRequest();
        createRequest.setDescription("Pago de servicios con factura incorrecta");
        createRequest.setAmount(new BigDecimal("1000000.00"));

        OrderResponse createdOrder = orderService.createOrder(createRequest, operator.getEmail());

        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.PENDING);

        Long orderId = createdOrder.getId();

        // PASO 2: ADMIN rechaza la orden con razón
        RejectOrderRequest rejectRequest = new RejectOrderRequest();
        rejectRequest.setRejectionReason("La factura adjunta no es legible");

        OrderResponse rejectedOrder = orderService.rejectOrder(orderId, rejectRequest, admin.getEmail());

        // Verificar que la orden fue rechazada correctamente
        assertThat(rejectedOrder).isNotNull();
        assertThat(rejectedOrder.getId()).isEqualTo(orderId);
        assertThat(rejectedOrder.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(rejectedOrder.getApprovedByEmail()).isEqualTo(admin.getEmail());
        assertThat(rejectedOrder.getApprovedAt()).isNotNull();

        // PASO 3: Intentar rechazar la orden nuevamente (debe fallar)
        RejectOrderRequest anotherReject = new RejectOrderRequest();
        anotherReject.setRejectionReason("Otra razón");

        assertThatThrownBy(() -> orderService.rejectOrder(orderId, anotherReject, admin.getEmail()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Solo se pueden rechazar órdenes en estado PENDING");
    }

    @WithMockUser(username = "admin-test@vortexbird.com", roles = "ADMIN")
    @Test
    @DisplayName("Flujo de error: Intentar aprobar orden inexistente")
    void testApproveNonExistentOrder() {
        Long nonExistentOrderId = 99999L;

        assertThatThrownBy(() -> orderService.approveOrder(nonExistentOrderId, admin.getEmail()))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @WithMockUser(username = "operator-test@vortexbird.com", roles = {"OPERATOR", "ADMIN"})
    @Test
    @DisplayName("Flujo múltiple: Crear varias órdenes y aprobar/rechazar selectivamente")
    void testMultipleOrdersFlow() {
        // Crear 3 órdenes
        CreateOrderRequest request1 = new CreateOrderRequest();
        request1.setDescription("Orden 1");
        request1.setAmount(new BigDecimal("1000000"));

        CreateOrderRequest request2 = new CreateOrderRequest();
        request2.setDescription("Orden 2");
        request2.setAmount(new BigDecimal("2000000"));

        CreateOrderRequest request3 = new CreateOrderRequest();
        request3.setDescription("Orden 3");
        request3.setAmount(new BigDecimal("3000000"));

        OrderResponse order1 = orderService.createOrder(request1, operator.getEmail());
        OrderResponse order2 = orderService.createOrder(request2, operator.getEmail());
        OrderResponse order3 = orderService.createOrder(request3, operator.getEmail());

        // Verificar que todas están en PENDING
        assertThat(order1.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order2.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order3.getStatus()).isEqualTo(OrderStatus.PENDING);

        // Aprobar order1
        OrderResponse approved1 = orderService.approveOrder(order1.getId(), admin.getEmail());
        assertThat(approved1.getStatus()).isEqualTo(OrderStatus.APPROVED);

        // Rechazar order2
        RejectOrderRequest rejectRequest = new RejectOrderRequest();
        rejectRequest.setRejectionReason("No cumple requisitos");
        OrderResponse rejected2 = orderService.rejectOrder(order2.getId(), rejectRequest, admin.getEmail());
        assertThat(rejected2.getStatus()).isEqualTo(OrderStatus.REJECTED);

        // Dejar order3 en PENDING
        OrderDetailResponse pending3 = orderService.getOrderById(order3.getId(), operator.getEmail());
        assertThat(pending3.getStatus()).isEqualTo(OrderStatus.PENDING);

        // Verificar estados finales
        assertThat(orderService.getOrderById(order1.getId(), admin.getEmail()).getStatus())
                .isEqualTo(OrderStatus.APPROVED);
        assertThat(orderService.getOrderById(order2.getId(), admin.getEmail()).getStatus())
                .isEqualTo(OrderStatus.REJECTED);
        assertThat(orderService.getOrderById(order3.getId(), admin.getEmail()).getStatus())
                .isEqualTo(OrderStatus.PENDING);
    }
}
