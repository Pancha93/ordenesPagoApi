package com.vortexbird.ordenesPago.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de login/autenticación.
 * Entrada del endpoint POST /api/auth/login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Credenciales de autenticación")
public class LoginRequest {
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Schema(description = "Email del usuario", example = "admin@vortexbird.com")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña del usuario", example = "password123")
    private String password;
}
