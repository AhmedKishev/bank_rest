package com.example.bankcards.security;


import com.example.bankcards.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtService {

    @Value("${jwt.secret}")
    String secret;

    @Value("${jwt.time")
    Duration lifeTime;

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        User user = (User) userDetails;
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());

        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + lifeTime.toMillis());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(issuedDate)
                .setExpiration(expiredDate)
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }


    public String getUsername(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public List<String> getRoles(String token) {
        return getClaimsFromToken(token).get("roles", List.class);
    }


    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())    // <-- Проверка подписи
                .build()                       // <-- Сборка парсера
                .parseSignedClaims(token)      // <-- Парсинг с проверкой подписи
                .getPayload();                 // <-- Получение claims
    }

}
