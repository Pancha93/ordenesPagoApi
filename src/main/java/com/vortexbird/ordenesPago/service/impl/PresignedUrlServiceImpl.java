package com.vortexbird.ordenesPago.service.impl;

import com.vortexbird.ordenesPago.service.PresignedUrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación del servicio de presigned URLs.
 * Simula el comportamiento de S3 presigned URLs generando tokens temporales.
 * Los tokens se almacenan en memoria (en producción con S3 real, esto no sería necesario).
 */
@Service
@Slf4j
public class PresignedUrlServiceImpl implements PresignedUrlService {
    
    /**
     * Almacén en memoria de tokens activos.
     * En una implementación real con S3, AWS maneja esto internamente.
     */
    private final Map<String, PresignedToken> activeTokens = new ConcurrentHashMap<>();
    
    @Override
    public String generatePresignedToken(String storageKey, int expirationMinutes) {
        // Generar token único (similar a como S3 genera presigned URLs)
        String token = UUID.randomUUID().toString();
        
        // Calcular fecha de expiración
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        
        // Almacenar token
        activeTokens.put(token, new PresignedToken(storageKey, expiresAt));
        
        log.debug("Generated presigned token for key: {} (expires in {} minutes)", 
                  storageKey, expirationMinutes);
        
        return token;
    }
    
    @Override
    public String validateAndGetStorageKey(String token) {
        PresignedToken presignedToken = activeTokens.get(token);
        
        if (presignedToken == null) {
            log.warn("Invalid token attempted: {}", token);
            throw new IllegalArgumentException("Token inválido o expirado");
        }
        
        if (presignedToken.isExpired()) {
            activeTokens.remove(token);
            log.warn("Expired token attempted: {}", token);
            throw new IllegalArgumentException("Token expirado");
        }
        
        log.debug("Token validated successfully: {}", token);
        return presignedToken.storageKey;
    }
    
    @Override
    public void invalidateToken(String token) {
        activeTokens.remove(token);
        log.debug("Token invalidated: {}", token);
    }
    
    @Override
    @Scheduled(fixedRate = 300000) // Cada 5 minutos
    public void cleanExpiredTokens() {
        int initialSize = activeTokens.size();
        activeTokens.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int removed = initialSize - activeTokens.size();
        
        if (removed > 0) {
            log.info("Cleaned {} expired tokens (remaining: {})", removed, activeTokens.size());
        }
    }
    
    /**
     * Clase interna para representar un token presigned
     */
    private static class PresignedToken {
        private final String storageKey;
        private final LocalDateTime expiresAt;
        
        public PresignedToken(String storageKey, LocalDateTime expiresAt) {
            this.storageKey = storageKey;
            this.expiresAt = expiresAt;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }
}
