package com.example.bankcards.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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