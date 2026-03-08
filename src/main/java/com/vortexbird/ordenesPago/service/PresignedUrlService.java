package com.vortexbird.ordenesPago.service;

/**
 * Servicio para gestionar presigned URLs simuladas (similar a S3).
 * Genera tokens temporales que permiten acceso a archivos por tiempo limitado.
 */
public interface PresignedUrlService {
    
    /**
     * Genera un token temporal para acceder a un archivo
     * @param storageKey Clave del archivo en el almacenamiento
     * @param expirationMinutes Minutos de validez del token
     * @return Token único para acceder al archivo
     */
    String generatePresignedToken(String storageKey, int expirationMinutes);
    
    /**
     * Valida un token y obtiene la clave del archivo asociado
     * @param token Token a validar
     * @return Clave del archivo si el token es válido
     * @throws IllegalArgumentException si el token es inválido o expiró
     */
    String validateAndGetStorageKey(String token);
    
    /**
     * Invalida un token (opcional, para limpieza manual)
     * @param token Token a invalidar
     */
    void invalidateToken(String token);
    
    /**
     * Limpia tokens expirados (tarea de mantenimiento)
     */
    void cleanExpiredTokens();
}
