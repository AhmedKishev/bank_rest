package com.example.bankcards.service.auth;

import com.example.bankcards.dto.jwt.JwtRequest;
import com.example.bankcards.dto.jwt.JwtResponse;
import com.example.bankcards.exception.AuthTokenNotValidException;
import com.example.bankcards.mapper.JwtMapper;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.UserDetailsServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    AuthenticationManager authenticationManager;
    UserDetailsServiceImpl userDetailsService;
    JwtService jwtService;

    @Override
    public JwtResponse createAuthToken(JwtRequest jwtRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(jwtRequest.getUsername(), jwtRequest.getPassword()));
        } catch (BadCredentialsException e) {
            throw new AuthTokenNotValidException(String.format("Пользователя с именем %s не существует", jwtRequest.getUsername()));
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(jwtRequest.getUsername());

        String token = jwtService.createToken(userDetails);
        return JwtMapper.toJwtResponse(token);
    }
}
