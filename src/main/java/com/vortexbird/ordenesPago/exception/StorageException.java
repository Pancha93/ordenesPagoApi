package com.vortexbird.ordenesPago.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando ocurre un error en el almacenamiento de archivos.
 * HTTP Status: 500 INTERNAL SERVER ERROR
 */
public class StorageException extends BusinessException {
    
    public StorageException(String message) {
        super("Error de almacenamiento: " + message,
              HttpStatus.INTERNAL_SERVER_ERROR,
              "STORAGE_ERROR");
    }
    
    public StorageException(String message, Throwable cause) {
        super("Error de almacenamiento: " + message,
              HttpStatus.INTERNAL_SERVER_ERROR,
              "STORAGE_ERROR",
              cause);
    }
}
