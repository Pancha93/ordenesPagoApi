package com.vortexbird.ordenesPago.repository;

import com.vortexbird.ordenesPago.entity.User;
import com.vortexbird.ordenesPago.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad User.
 * 
 * Consultas personalizadas:
 * - Buscar por email (para autenticación)
 * - Buscar por rol
 * - Buscar usuarios activos
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca un usuario por email (usado en autenticación)
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca usuarios por rol
     */
    List<User> findByRole(UserRole role);
    
    /**
     * Busca usuarios activos
     */
    List<User> findByActiveTrue();
    
    /**
     * Busca usuarios por rol y estado activo
     */
    List<User> findByRoleAndActiveTrue(UserRole role);
}
