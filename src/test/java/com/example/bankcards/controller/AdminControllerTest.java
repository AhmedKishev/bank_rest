package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardDtoBlock;
import com.example.bankcards.service.card.admin.AdminCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminCardService cardService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        objectMapper = new ObjectMapper();
    }


    @Test
    void getAllCards_WhenNoCards_ShouldReturnEmptyList() throws Exception {
        when(cardService.getAllCards()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(cardService, times(1)).getAllCards();
    }

    @Test
    void createCardForUser_ShouldCallService() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/create")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cardService, times(1)).createCardForUser();
    }

    @Test
    void blockCardForUsers_ShouldCallService() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/block")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cardService, times(1)).blockCardForUsers();
    }

    @Test
    void removeCardForUser_WithValidRequest_ShouldCallService() throws Exception {
        CardDtoBlock request = CardDtoBlock.builder()
                .userId(1L)
                .cardNumber("1234567812345678")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/v1/admin/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(cardService, times(1)).removeCardForUser(any(CardDtoBlock.class));
    }

    @Test
    void removeCardForUser_WithNullUserId_ShouldReturnBadRequest() throws Exception {
        CardDtoBlock request = CardDtoBlock.builder()
                .userId(null)
                .cardNumber("1234567812345678")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);


        mockMvc.perform(patch("/api/v1/admin/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).removeCardForUser(any());
    }

    @Test
    void removeCardForUser_WithBlankCardNumber_ShouldReturnBadRequest() throws Exception {
        CardDtoBlock request = CardDtoBlock.builder()
                .userId(1L)
                .cardNumber("")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);


        mockMvc.perform(patch("/api/v1/admin/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).removeCardForUser(any());
    }

    @Test
    void removeCardForUser_WithMissingCardNumber_ShouldReturnBadRequest() throws Exception {

        String requestBody = "{\"userId\": 1}";

        mockMvc.perform(patch("/api/v1/admin/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).removeCardForUser(any());
    }

    @Test
    void removeCardForUser_WithEmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).removeCardForUser(any());
    }

    @Test
    void removeCardForUser_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid}"))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).removeCardForUser(any());
    }

    @Test
    void removeCardForUser_ShouldPassCorrectDtoToService() throws Exception {
        CardDtoBlock expectedDto = CardDtoBlock.builder()
                .userId(1L)
                .cardNumber("1234567812345678")
                .build();

        String requestBody = objectMapper.writeValueAsString(expectedDto);

        mockMvc.perform(patch("/api/v1/admin/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(cardService, times(1)).removeCardForUser(argThat(dto ->
                dto.getUserId().equals(1L) &&
                        dto.getCardNumber().equals("1234567812345678")
        ));
    }
}