package com.vortexbird.ordenesPago.config;

import com.vortexbird.ordenesPago.entity.User;
import com.vortexbird.ordenesPago.enums.UserRole;
import com.vortexbird.ordenesPago.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración para inicializar datos de prueba en la base de datos.
 * Crea usuarios ADMIN y OPERATOR si no existen.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Crear usuario ADMIN si no existe
            if (!userRepository.existsByEmail("admin@vortexbird.com")) {
                User admin = User.builder()
                        .email("admin@vortexbird.com")
                        .password(passwordEncoder.encode("password123"))
                        .fullName("Juan Admin")
                        .role(UserRole.ADMIN)
                        .active(true)
                        .build();
                userRepository.save(admin);
                log.info("Usuario ADMIN creado: admin@vortexbird.com");
            }
            
            // Crear usuario OPERATOR si no existe
            if (!userRepository.existsByEmail("operator@vortexbird.com")) {
                User operator = User.builder()
                        .email("operator@vortexbird.com")
                        .password(passwordEncoder.encode("password123"))
                        .fullName("María Operadora")
                        .role(UserRole.OPERATOR)
                        .active(true)
                        .build();
                userRepository.save(operator);
                log.info("Usuario OPERATOR creado: operator@vortexbird.com");
            }

            // Crear usuario OPERATOR si no existe
            if (!userRepository.existsByEmail("operator2@vortexbird.com")) {
                User operator = User.builder()
                        .email("operator2@vortexbird.com")
                        .password(passwordEncoder.encode("123456"))
                        .fullName("Alison Operadora")
                        .role(UserRole.OPERATOR)
                        .active(true)
                        .build();
                userRepository.save(operator);
                log.info("Usuario OPERATOR creado: operator2@vortexbird.com");
            }
            
            log.info("=================================================");
            log.info("Usuarios de prueba listos:");
            log.info("ADMIN: admin@vortexbird.com / password123");
            log.info("OPERATOR: operator@vortexbird.com / password123");
            log.info("OPERATOR: operator2@vortexbird.com / 123456");
            log.info("=================================================");
        };
    }
}
