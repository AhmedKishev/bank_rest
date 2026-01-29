package com.example.bankcards.security;


import com.example.bankcards.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.*;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtService {

    @Value("${jwt.secret}")
    String secret;

    @Value("${jwt.time}")
    Duration lifeTime;

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createToken(User user) {
        Map<String, Object> claims = new HashMap<>();

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
        Claims claims = getClaimsFromToken(token);

        String role = claims.get("role", String.class);
        if (role != null) {
            return Collections.singletonList(role);
        }
        return Collections.emptyList();
    }


    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
