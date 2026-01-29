package com.example.bankcards.dto.jwt;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    String token;
}
