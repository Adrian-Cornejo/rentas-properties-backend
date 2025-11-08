package com.rentas.properties.business.services;

import com.rentas.properties.api.dto.request.LoginRequest;
import com.rentas.properties.api.dto.request.RefreshTokenRequest;
import com.rentas.properties.api.dto.request.RegisterRequest;
import com.rentas.properties.api.dto.response.AuthResponse;


public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String token);

    boolean validateToken(String token);
}