package com.example.bankcards.service;

import com.example.bankcards.dto.UserDtoIn;
import com.example.bankcards.dto.UserDtoOut;

public interface UserService {

    UserDtoOut createNewUser(UserDtoIn userDtoIn);

}
