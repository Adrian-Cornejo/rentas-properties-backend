package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.request.LoginRequest;
import com.rentas.properties.api.dto.request.RefreshTokenRequest;
import com.rentas.properties.api.dto.request.RegisterRequest;
import com.rentas.properties.api.dto.response.AuthResponse;
import com.rentas.properties.api.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Authentication", description = "Endpoints para autenticación y registro de usuarios")
public interface AuthController {

    @Operation(
            summary = "Registrar nuevo usuario",
            description = """
                    Registra un nuevo usuario en el sistema.
                    
                    **Importante**: El usuario se registra SIN organización. 
                    Después del registro debe:
                    1. Iniciar sesión para obtener el token
                    2. Unirse a una organización usando un código de invitación
                    
                    **Roles disponibles**: ADMIN, MANAGER, VIEWER
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o el email ya está registrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request);

    @Operation(
            summary = "Iniciar sesión",
            description = """
                    Inicia sesión con email y contraseña.
                    
                    Retorna:
                    - **accessToken**: Token JWT para autenticación (válido 24 horas)
                    - **refreshToken**: Token para renovar el access token (válido 7 días)
                    
                    Usa el accessToken en el header: `Authorization: Bearer {accessToken}`
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);

    @Operation(
            summary = "Renovar token de acceso",
            description = """
                    Renueva el access token usando el refresh token.
                    
                    Cuando el access token expire, usa este endpoint para obtener uno nuevo
                    sin necesidad de volver a iniciar sesión.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token renovado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token inválido o expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request);

    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida los tokens del usuario actual"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    ResponseEntity<Void> logout(@RequestHeader("Authorization") String token);

    @Operation(
            summary = "Validar token",
            description = "Verifica si un token es válido"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token);
}