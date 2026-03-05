package com.vortexbird.ordenesPago.service.impl;

import com.vortexbird.ordenesPago.dto.external.ApprovalNotificationDto;
import com.vortexbird.ordenesPago.entity.Order;
import com.vortexbird.ordenesPago.exception.ExternalIntegrationException;
import com.vortexbird.ordenesPago.service.ExternalNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementación del servicio de notificaciones externas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalNotificationServiceImpl implements ExternalNotificationService {
    
    private final WebClient externalWebClient;
    
    @Value("${external.api.notification-endpoint}")
    private String notificationEndpoint;
    
    @Override
    public void notifyOrderApproved(Order order) {
        log.info("Notifying external system for approved order: {}", order.getId());
        
        ApprovalNotificationDto payload = ApprovalNotificationDto.builder()
                .orderId(order.getId())
                .status(order.getStatus().name())
                .description(order.getDescription())
                .amount(order.getAmount())
                .approvedAt(order.getApprovedAt())
                .approvedBy(order.getApprovedBy().getEmail())
                .build();
        
        externalWebClient.post()
                .uri(notificationEndpoint)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(response -> {
                    log.info("Successfully notified external system for order {}: {}", 
                            order.getId(), response);
                })
                .doOnError(error -> {
                    log.error("Failed to notify external system for order {}: {}", 
                            order.getId(), error.getMessage());
                })
                .onErrorResume(error -> {
                    // No propagar error, solo loggear
                    return Mono.empty();
                })
                .subscribe(); // Fire-and-forget
    }
}
