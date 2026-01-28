package com.example.bankcards.dto.jwt;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtRequest {

    @NotBlank
    String username;

    @NotBlank
    String password;
}
