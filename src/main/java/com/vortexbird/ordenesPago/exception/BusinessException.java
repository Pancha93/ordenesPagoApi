package com.vortexbird.ordenesPago.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción base para todas las excepciones de negocio.
 * Todas las excepciones personalizadas deben heredar de esta clase.
 * 
 * Proporciona:
 * - Código de estado HTTP apropiado
 * - Código de error para el frontend
 * - Mensaje descriptivo
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final HttpStatus httpStatus;
    private final String errorCode;
    
    public BusinessException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
    
    public BusinessException(String message, HttpStatus httpStatus, String errorCode, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
}
