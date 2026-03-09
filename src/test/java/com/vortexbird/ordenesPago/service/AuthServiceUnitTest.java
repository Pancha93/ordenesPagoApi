package com.vortexbird.ordenesPago.service;

import com.vortexbird.ordenesPago.dto.request.LoginRequest;
import com.vortexbird.ordenesPago.dto.response.AuthResponse;
import com.vortexbird.ordenesPago.entity.User;
import com.vortexbird.ordenesPago.enums.UserRole;
import com.vortexbird.ordenesPago.repository.UserRepository;
import com.vortexbird.ordenesPago.security.JwtTokenProvider;
import com.vortexbird.ordenesPago.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitario para AuthService.
 * Prueba la lógica de autenticación y generación de JWT.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service - Tests Unitarios")
class AuthServiceUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User adminUser;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .email("admin@vortexbird.com")
                .fullName("Admin User")
                .role(UserRole.ADMIN)
                .build();

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("admin@vortexbird.com");
        validLoginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Login exitoso - Retorna token y datos del usuario")
    void testLoginSuccess() {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                adminUser.getEmail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(adminUser.getEmail()))
                .thenReturn(Optional.of(adminUser));
        when(jwtTokenProvider.createToken(adminUser.getEmail(), adminUser.getRole()))
                .thenReturn("mock-jwt-token-1234567890");

        // Act
        AuthResponse response = authService.login(validLoginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-jwt-token-1234567890");
        assertThat(response.getEmail()).isEqualTo("admin@vortexbird.com");
        assertThat(response.getFullName()).isEqualTo("Admin User");
        assertThat(response.getRole()).isEqualTo(UserRole.ADMIN);

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, times(1)).createToken(adminUser.getEmail(), adminUser.getRole());
        verify(userRepository, times(1)).findByEmail(adminUser.getEmail());
    }

    @Test
    @DisplayName("Login fallido - Credenciales inválidas")
    void testLoginFailedInvalidCredentials() {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("admin@vortexbird.com");
        invalidRequest.setPassword("wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(invalidRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).createToken(any(), any());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Login exitoso - Usuario OPERATOR")
    void testLoginSuccessOperator() {
        // Arrange
        User operatorUser = User.builder()
                .id(2L)
                .email("operator@vortexbird.com")
                .fullName("Operator User")
                .role(UserRole.OPERATOR)
                .build();

        LoginRequest operatorLogin = new LoginRequest();
        operatorLogin.setEmail("operator@vortexbird.com");
        operatorLogin.setPassword("password123");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                operatorUser.getEmail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_OPERATOR"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(operatorUser.getEmail()))
                .thenReturn(Optional.of(operatorUser));
        when(jwtTokenProvider.createToken(operatorUser.getEmail(), operatorUser.getRole()))
                .thenReturn("operator-jwt-token");

        // Act
        AuthResponse response = authService.login(operatorLogin);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("operator-jwt-token");
        assertThat(response.getEmail()).isEqualTo("operator@vortexbird.com");
        assertThat(response.getRole()).isEqualTo(UserRole.OPERATOR);
    }
}
