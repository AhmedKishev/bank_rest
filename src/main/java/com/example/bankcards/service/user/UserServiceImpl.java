package com.example.bankcards.service.user;

import com.example.bankcards.dto.user.UserDtoIn;
import com.example.bankcards.dto.user.UserDtoOut;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDtoOut createNewUser(UserDtoIn userDtoIn) {
        if (userRepository.findByUsername(userDtoIn.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException(String.format("Пользователь с именем: %s уже существует", userDtoIn.getUsername()));
        }

        User saveUser = UserMapper.toUser(userDtoIn);
        saveUser.setPassword(passwordEncoder.encode(userDtoIn.getPassword()));
        userRepository.save(saveUser);
        return UserMapper.toUserDtoOut(saveUser);
    }

    @Transactional(readOnly = true)
    public User getEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователя с Id %d не существует", userId)));
    }

}
