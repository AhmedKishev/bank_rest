package com.example.bankcards.mapper;

import com.example.bankcards.dto.user.UserDtoIn;
import com.example.bankcards.dto.user.UserDtoOut;
import com.example.bankcards.dto.enums.Role;
import com.example.bankcards.entity.User;

public class UserMapper {

    public static UserDtoOut toUserDtoOut(User user) {
        return UserDtoOut.builder()
                .name(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(UserDtoIn userDtoIn) {
        return User.builder()
                .role(Role.USER)
                .email(userDtoIn.getEmail())
                .password(userDtoIn.getPassword())
                .username(userDtoIn.getUsername())
                .build();
    }

}
