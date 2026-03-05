package com.vortexbird.ordenesPago.security;

import com.vortexbird.ordenesPago.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Componente responsable de generar y validar tokens JWT.
 * 
 * Funcionalidades:
 * - Generar token JWT con claims (email, rol)
 * - Validar firma y expiración del token
 * - Extraer información del token (email, rol)
 * 
 * Algoritmo: HS512 (HMAC with SHA-512)
 * Claims incluidos: email (subject), role
 * 
 * Configuración desde application.properties:
 * - jwt.secret: Clave secreta (mínimo 512 bits)
 * - jwt.expiration: Tiempo de expiración en milisegundos
 */
@Component
@Slf4j
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secretKeyString;
    
    @Value("${jwt.expiration}")
    private long validityInMilliseconds;
    
    private SecretKey secretKey;
    
    @PostConstruct
    protected void init() {
        // Convertir string a SecretKey para JJWT 0.12+
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        log.info("JWT Token Provider initialized with expiration: {} ms", validityInMilliseconds);
    }
    
    /**
     * Genera un token JWT para un usuario
     */
    public String createToken(String email, UserRole role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        
        return Jwts.builder()
                .subject(email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }
    
    /**
     * Valida un token JWT
     * @return true si el token es válido (firma correcta y no expirado)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extrae el email (subject) del token
     */
    public String getEmail(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }
    
    /**
     * Extrae el rol del token
     */
    public UserRole getRole(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        String roleString = claims.get("role", String.class);
        return UserRole.valueOf(roleString);
    }
    
    /**
     * Obtiene el tiempo de expiración configurado
     */
    public long getValidityInMilliseconds() {
        return validityInMilliseconds;
    }
}
