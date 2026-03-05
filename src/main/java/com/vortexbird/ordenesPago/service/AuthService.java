package com.vortexbird.ordenesPago.service;

import com.vortexbird.ordenesPago.dto.request.LoginRequest;
import com.vortexbird.ordenesPago.dto.response.AuthResponse;

/**
 * Servicio para manejo de autenticación.
 */
public interface AuthService {
    
    /**
     * Autentica un usuario y genera un token JWT
     */
    AuthResponse login(LoginRequest request);
}
