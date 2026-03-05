package com.vortexbird.ordenesPago.security;

import com.vortexbird.ordenesPago.entity.User;
import com.vortexbird.ordenesPago.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Implementación de UserDetailsService de Spring Security.
 * 
 * Responsabilidad:
 * - Cargar usuario desde la base de datos por email
 * - Convertir entidad User a UserDetails de Spring Security
 * - Asignar authorities basados en el rol (ROLE_ADMIN, ROLE_OPERATOR)
 * 
 * Usado por Spring Security para autenticación y autorización.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + email));
        
        if (!user.getActive()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }
        
        // Convertir rol a authority de Spring Security
        // El prefijo "ROLE_" es requerido por Spring Security
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(authority))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getActive())
                .build();
    }
}
