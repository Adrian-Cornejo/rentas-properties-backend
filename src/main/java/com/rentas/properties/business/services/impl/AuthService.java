package com.rentas.properties.business.services.impl;

import com.rentas.properties.api.dto.request.LoginRequest;
import com.rentas.properties.api.dto.request.RegisterRequest;
import com.rentas.properties.api.dto.response.AuthResponse;
import com.rentas.properties.api.exception.EmailAlreadyExistsException;
import com.rentas.properties.security.jwt.JwtService;
import com.rentas.properties.dao.entity.User;
import com.rentas.properties.dao.repository.UserRepository;
import com.rentas.properties.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
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

        UserDetailsImpl userDetails = UserDetailsImpl.build(savedUser);
        String jwtToken = jwtService.generateToken(userDetails, savedUser.getId(), savedUser.getRole());

        return buildAuthResponse(jwtToken, savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String jwtToken = jwtService.generateToken(userDetails, user.getId(), user.getRole());

        return buildAuthResponse(jwtToken, user);
    }

    private AuthResponse buildAuthResponse(String token, User user) {
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
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(userDto)
                .build();
    }
}