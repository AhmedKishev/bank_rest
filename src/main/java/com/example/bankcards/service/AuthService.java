package com.example.bankcards.service;

import com.example.bankcards.dto.JwtRequest;
import com.example.bankcards.dto.JwtResponse;
import jakarta.validation.Valid;

public interface AuthService {
    JwtResponse createAuthToken(@Valid JwtRequest jwtRequest);
}
