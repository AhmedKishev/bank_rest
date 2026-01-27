package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.UserDtoIn;
import com.example.bankcards.dto.UserDtoOut;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class AuthController {

    UserService userService;
    AuthService authService;

    @PostMapping("/registration")
    public UserDtoOut registrationNewUser(@RequestBody @Valid UserDtoIn userDtoIn) {
        return userService.createNewUser(userDtoIn);
    }

    @PostMapping("/auth")
    public JwtResponse createAuthToken(@RequestBody @Valid JwtRequest jwtRequest) {
        return authService.createAuthToken(jwtRequest);
    }
}
