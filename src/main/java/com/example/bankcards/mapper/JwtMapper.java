package com.example.bankcards.mapper;

import com.example.bankcards.dto.jwt.JwtResponse;

public class JwtMapper {

    public static JwtResponse toJwtResponse(String token) {
        return JwtResponse.builder()
                .token(token)
                .build();
    }

}
