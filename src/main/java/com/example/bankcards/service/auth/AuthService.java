package com.example.bankcards.service.auth;

import com.example.bankcards.dto.jwt.JwtRequest;
import com.example.bankcards.dto.jwt.JwtResponse;
import jakarta.validation.Valid;

public interface AuthService {
    JwtResponse createAuthToken(@Valid JwtRequest jwtRequest);
}
