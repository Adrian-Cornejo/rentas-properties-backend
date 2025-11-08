package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.request.LoginRequest;
import com.rentas.properties.api.dto.request.RefreshTokenRequest;
import com.rentas.properties.api.dto.request.RegisterRequest;
import com.rentas.properties.api.dto.response.AuthResponse;
import com.rentas.properties.api.exception.EmailAlreadyExistsException;
import com.rentas.properties.api.exception.InvalidTokenException;
import com.rentas.properties.api.exception.UserNotFoundException;
import com.rentas.properties.business.services.AuthService;
import com.rentas.properties.dao.entity.User;
import com.rentas.properties.dao.repository.UserRepository;
import com.rentas.properties.security.jwt.JwtService;
import com.rentas.properties.security.service.UserDetailsImpl;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;


    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registrando nuevo usuario: {}", request.getEmail());


        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "El email " + request.getEmail() + " ya está registrado"
            );
        }


        String role = request.getRole() != null && !request.getRole().isEmpty()
                ? request.getRole().toUpperCase()
                : "USER";

        if (!role.equals("USER") && !role.equals("ADMIN")) {
            throw new IllegalArgumentException("Rol inválido. Debe ser USER o ADMIN");
        }
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(role)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuario creado exitosamente con ID: {}", savedUser.getId());

        UserDetailsImpl userDetails = UserDetailsImpl.build(savedUser);

        String accessToken = jwtService.generateToken(userDetails, savedUser.getId(), savedUser.getRole());
        String refreshToken = generateRefreshToken(userDetails, savedUser.getId(), savedUser.getRole());

        return buildAuthResponse(accessToken, refreshToken, savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Procesando login para: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateToken(userDetails, user.getId(), user.getRole());
        String refreshToken = generateRefreshToken(userDetails, user.getId(), user.getRole());

        log.info("Login exitoso para usuario: {}", user.getId());

        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Procesando refresh token");

        String refreshToken = request.getRefreshToken();

        if (blacklistedTokens.contains(refreshToken)) {
            throw new InvalidTokenException("El refresh token ha sido invalidado");
        }

        try {
            String username = jwtService.extractUsername(refreshToken);
            UUID userId = jwtService.extractUserId(refreshToken);
            String role = jwtService.extractRole(refreshToken);

            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

            if (!jwtService.isTokenValid(refreshToken, userDetails)) {
                throw new InvalidTokenException("Refresh token inválido o expirado");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

            if (!user.getIsActive()) {
                throw new InvalidTokenException("El usuario está desactivado");
            }

            String newAccessToken = jwtService.generateToken(userDetails, userId, role);

            log.info("Token refrescado exitosamente para usuario: {}", userId);

            return buildAuthResponse(newAccessToken, refreshToken, user);

        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error al procesar refresh token: {}", e.getMessage());
            throw new InvalidTokenException("Refresh token inválido");
        }
    }

    @Override
    public void logout(String token) {
        log.info("Procesando logout");

        String cleanToken = cleanToken(token);

        blacklistedTokens.add(cleanToken);

        try {
            String username = jwtService.extractUsername(cleanToken);
            log.info("Logout exitoso para usuario: {}", username);
        } catch (Exception e) {
            log.warn("Error extrayendo usuario del token durante logout: {}", e.getMessage());
        }
    }

    @Override
    public boolean validateToken(String token) {
        log.debug("Validando token");

        String cleanToken = cleanToken(token);

        if (blacklistedTokens.contains(cleanToken)) {
            return false;
        }

        try {
            String username = jwtService.extractUsername(cleanToken);
            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
            return jwtService.isTokenValid(cleanToken, userDetails);
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    private String generateRefreshToken(UserDetailsImpl userDetails, UUID userId, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("type", "refresh");

        return jwtService.generateToken(extraClaims, userDetails, userId, role);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        AuthResponse.UserDto userDto = AuthResponse.UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .lastLogin(user.getLastLogin())
                .build();

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(userDto)
                .build();
    }


    private String cleanToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}