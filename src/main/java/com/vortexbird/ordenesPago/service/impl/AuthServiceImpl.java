package com.vortexbird.ordenesPago.service.impl;

import com.vortexbird.ordenesPago.dto.request.LoginRequest;
import com.vortexbird.ordenesPago.dto.response.AuthResponse;
import com.vortexbird.ordenesPago.entity.User;
import com.vortexbird.ordenesPago.repository.UserRepository;
import com.vortexbird.ordenesPago.security.JwtTokenProvider;
import com.vortexbird.ordenesPago.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de autenticación.
 * 
 * Responsabilidades:
 * - Validar credenciales usando AuthenticationManager
 * - Generar token JWT
 * - Retornar información del usuario autenticado
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getEmail());
        
        // Autenticar con Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        // Cargar usuario desde BD
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        
        // Generar token JWT
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
        
        log.info("User '{}' logged in successfully with role: {}", user.getEmail(), user.getRole());
        
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .expiresIn(jwtTokenProvider.getValidityInMilliseconds())
                .build();
    }
}
