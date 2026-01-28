package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.UserDtoIn;
import com.example.bankcards.dto.user.UserDtoOut;

public interface UserService {

    UserDtoOut createNewUser(UserDtoIn userDtoIn);

}
