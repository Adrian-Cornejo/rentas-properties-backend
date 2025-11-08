package com.rentas.properties.api.controller.impl;



import com.rentas.properties.api.controller.AuthController;
import com.rentas.properties.api.dto.request.LoginRequest;
import com.rentas.properties.api.dto.request.RefreshTokenRequest;
import com.rentas.properties.api.dto.request.RegisterRequest;
import com.rentas.properties.api.dto.response.AuthResponse;

import com.rentas.properties.business.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    @Override
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Nuevo registro de usuario: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        log.info("Usuario registrado exitosamente: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Intento de login para: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        log.info("Login exitoso para: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Solicitud de refresh token");
        AuthResponse response = authService.refreshToken(request);
        log.info("Token refrescado exitosamente");
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        log.info("Solicitud de logout");
        authService.logout(token);
        log.info("Logout exitoso");
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        log.debug("Validando token");
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }
}