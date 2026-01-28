package com.example.bankcards.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDtoIn {

    @NotBlank
    String username;

    @NotBlank
    String password;

    @NotBlank
    String confirmedPassword;

    @NotBlank
    @Email
    String email;

}