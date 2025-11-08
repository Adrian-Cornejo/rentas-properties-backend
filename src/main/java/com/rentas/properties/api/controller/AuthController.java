package com.rentas.properties.api.controller;

import com.rentas.properties.api.dto.request.LoginRequest;
import com.rentas.properties.api.dto.request.RefreshTokenRequest;
import com.rentas.properties.api.dto.request.RegisterRequest;
import com.rentas.properties.api.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Authentication", description = "Endpoints para autenticación y autorización")
public interface AuthController {

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Registra un nuevo usuario en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request);

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario y devuelve tokens de acceso"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "403", description = "Usuario desactivado")
    })
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);

    @Operation(
            summary = "Refrescar token",
            description = "Genera un nuevo token de acceso usando el refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refrescado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
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