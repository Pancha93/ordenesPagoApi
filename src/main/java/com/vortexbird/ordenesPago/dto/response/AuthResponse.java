package com.vortexbird.ordenesPago.dto.response;

import com.vortexbird.ordenesPago.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para autenticación exitosa.
 * Salida del endpoint POST /api/auth/login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta de autenticación con token JWT")
public class AuthResponse {
    
    @Schema(description = "Token JWT para Bearer Authentication", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String token;
    
    @Schema(description = "Tipo de token", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";
    
    @Schema(description = "Email del usuario autenticado", example = "admin@vortexbird.com")
    private String email;
    
    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
    private String fullName;
    
    @Schema(description = "Rol del usuario", example = "ADMIN")
    private UserRole role;
    
    @Schema(description = "Tiempo de expiración en milisegundos", example = "3600000")
    private Long expiresIn;
}
