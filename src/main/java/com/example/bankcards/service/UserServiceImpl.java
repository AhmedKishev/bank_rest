package com.example.bankcards.service;

import com.example.bankcards.dto.UserDtoIn;
import com.example.bankcards.dto.UserDtoOut;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;


    @Override
    @Transactional
    public UserDtoOut createNewUser(UserDtoIn userDtoIn) {
        if (userRepository.findByUsername(userDtoIn.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException(String.format("Пользователь с именем: %s уже существует", userDtoIn.getUsername()));
        }

        User saveUser = userRepository.save(UserMapper.toUser(userDtoIn));
        return UserMapper.toUserDtoOut(saveUser);
    }



}
