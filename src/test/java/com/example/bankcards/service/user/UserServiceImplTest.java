package com.example.bankcards.service.user;

import com.example.bankcards.dto.enums.Role;
import com.example.bankcards.dto.user.UserDtoIn;
import com.example.bankcards.dto.user.UserDtoOut;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDtoIn validUserDtoIn;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validUserDtoIn = UserDtoIn.builder()
                .username("john_doe")
                .password("password123")
                .confirmedPassword("password123")
                .email("john@example.com")
                .build();

        savedUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("encodedPassword123")
                .role(Role.USER)
                .cards(Collections.emptyList())
                .build();
    }

    @Test
    void createNewUser_WithValidData_ShouldCreateUserSuccessfully() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);


        UserDtoOut result = userService.createNewUser(validUserDtoIn);


        assertNotNull(result);
        assertEquals("john_doe", result.getName());
        assertEquals("john@example.com", result.getEmail());


        verify(userRepository, times(1)).findByUsername("john_doe");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createNewUser_WithExistingUsername_ShouldThrowUsernameAlreadyExistsException() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(savedUser));


        UsernameAlreadyExistsException exception = assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userService.createNewUser(validUserDtoIn)
        );

        assertEquals("Пользователь с именем: john_doe уже существует", exception.getMessage());

        verify(userRepository, times(1)).findByUsername("john_doe");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createNewUser_ShouldEncodePasswordBeforeSaving() {

        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";

        validUserDtoIn.setPassword(rawPassword);
        validUserDtoIn.setConfirmedPassword(rawPassword);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            assertEquals(encodedPassword, userToSave.getPassword());
            userToSave.setId(1L);
            return userToSave;
        });


        userService.createNewUser(validUserDtoIn);

        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(argThat(user ->
                encodedPassword.equals(user.getPassword())
        ));
    }

    @Test
    void createNewUser_ShouldSetDefaultRoleAsUser() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            assertEquals(Role.USER, userToSave.getRole());
            userToSave.setId(1L);
            return userToSave;
        });


        userService.createNewUser(validUserDtoIn);

        verify(userRepository, times(1)).save(argThat(user ->
                user.getRole() == Role.USER
        ));
    }


    @Test
    void getEntityById_WithExistingUserId_ShouldReturnUser() {

        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));

        User result = userService.getEntityById(userId);

        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(savedUser.getUsername(), result.getUsername());
        assertEquals(savedUser.getEmail(), result.getEmail());
        assertEquals(savedUser.getRole(), result.getRole());
        assertNotNull(result.getCards());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getEntityById_WithNonExistingUserId_ShouldThrowUserNotFoundException() {

        Long nonExistingUserId = 999L;
        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getEntityById(nonExistingUserId)
        );

        assertEquals("Пользователя с Id 999 не существует", exception.getMessage());
        verify(userRepository, times(1)).findById(nonExistingUserId);
    }

    @Test
    void getEntityById_WithNullUserId_ShouldThrowUserNotFoundException() {

        Long nullUserId = null;
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getEntityById(nullUserId)
        );

        assertEquals("Пользователя с Id null не существует", exception.getMessage());
        verify(userRepository, times(1)).findById(null);
    }

    @Test
    void getEntityById_ShouldReturnEntityNotDto() {

        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));


        User result = userService.getEntityById(userId);

        assertInstanceOf(User.class, result);
        assertEquals(User.class, result.getClass());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void createNewUser_WithDifferentEmail_ShouldWorkCorrectly() {

        UserDtoIn anotherUserDtoIn = UserDtoIn.builder()
                .username("jane_smith")
                .password("securePass456")
                .confirmedPassword("securePass456")
                .email("jane.smith@example.com")
                .build();

        User anotherSavedUser = User.builder()
                .id(2L)
                .username("jane_smith")
                .email("jane.smith@example.com")
                .password("encodedSecurePass456")
                .role(Role.USER)
                .cards(Collections.emptyList())
                .build();

        when(userRepository.findByUsername("jane_smith")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("securePass456")).thenReturn("encodedSecurePass456");
        when(userRepository.save(any(User.class))).thenReturn(anotherSavedUser);


        UserDtoOut result = userService.createNewUser(anotherUserDtoIn);


        assertNotNull(result);
        assertEquals("jane_smith", result.getName()); // username должен стать name в DTO
        assertEquals("jane.smith@example.com", result.getEmail());

        verify(userRepository, times(1)).findByUsername("jane_smith");
        verify(passwordEncoder, times(1)).encode("securePass456");
    }

    @Test
    void getEntityById_WithZeroUserId_ShouldThrowUserNotFoundException() {

        Long zeroUserId = 0L;
        when(userRepository.findById(zeroUserId)).thenReturn(Optional.empty());


        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getEntityById(zeroUserId)
        );

        assertEquals("Пользователя с Id 0 не существует", exception.getMessage());
        verify(userRepository, times(1)).findById(zeroUserId);
    }

    @Test
    void getEntityById_WithNegativeUserId_ShouldThrowUserNotFoundException() {

        Long negativeUserId = -1L;
        when(userRepository.findById(negativeUserId)).thenReturn(Optional.empty());


        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getEntityById(negativeUserId)
        );

        assertEquals("Пользователя с Id -1 не существует", exception.getMessage());
        verify(userRepository, times(1)).findById(negativeUserId);
    }

    @Test
    void createNewUser_ShouldCheckUsernameUniqueness() {

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);


        userService.createNewUser(validUserDtoIn);


        verify(userRepository, times(1)).findByUsername("john_doe");
    }

    @Test
    void createNewUser_ShouldMapAllFieldsFromDtoToEntity() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);


        userService.createNewUser(validUserDtoIn);


        verify(userRepository, times(1)).save(argThat(user ->
                user.getUsername().equals(validUserDtoIn.getUsername()) &&
                        user.getEmail().equals(validUserDtoIn.getEmail())
        ));
    }

    @Test
    void createNewUser_ShouldReturnDtoWithCorrectFields() {

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);


        UserDtoOut result = userService.createNewUser(validUserDtoIn);


        assertNotNull(result);

        assertEquals(validUserDtoIn.getUsername(), result.getName());
        assertEquals(validUserDtoIn.getEmail(), result.getEmail());


        var fields = UserDtoOut.class.getDeclaredFields();
        assertEquals(2, fields.length);


        var fieldNames = java.util.Arrays.stream(fields)
                .map(java.lang.reflect.Field::getName)
                .sorted()
                .toList();
        assertEquals(List.of("email", "name"), fieldNames);
    }
}