package com.example.bankcards.controller;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.dto.enums.CardStatus;
import com.example.bankcards.service.card.admin.AdminCardService;
import com.example.bankcards.service.card.user.UserCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserCardControllerTest {

    @Mock
    private UserCardService cardService;

    @Mock
    private AdminCardService adminCardService;

    @InjectMocks
    private UserCardController userCardController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userCardController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void getAllCardsForUser_ShouldReturnPaginatedCards() throws Exception {
        Long userId = 1L;
        int page = 0;
        int size = 10;

        CardDtoOutUser card = CardDtoOutUser.builder()
                .cardHolder("John Doe")
                .expiryDate(LocalDate.now().plusYears(3))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .cardNumberMasked("**** **** **** 1234")
                .build();

        List<CardDtoOutUser> cards = Collections.singletonList(card);
        Page<CardDtoOutUser> pageResult = new PageImpl<>(cards, PageRequest.of(page, size), 1);

        when(cardService.getAllCardsForUser(eq(userId), eq(page), eq(size)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/user/cards")
                        .header("X-USER-ID", userId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardHolder").value("John Doe"))
                .andExpect(jsonPath("$.content[0].balance").value(1000.00))
                .andExpect(jsonPath("$.content[0].cardNumberMasked").value("**** **** **** 1234"));

        verify(cardService, times(1)).getAllCardsForUser(userId, page, size);
    }

    @Test
    void getAllCardsForUser_WithDefaultPagination_ShouldUseDefaults() throws Exception {
        Long userId = 1L;
        Page<CardDtoOutUser> emptyPage = Page.empty();

        when(cardService.getAllCardsForUser(eq(userId), eq(0), eq(10)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/user/cards")
                        .header("X-USER-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cardService, times(1)).getAllCardsForUser(userId, 0, 10);
    }

    @Test
    void getAllCardsForUser_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/user/cards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestCreateCard_WithValidRequest_ShouldCallService() throws Exception {
        CardDtoIn request = CardDtoIn.builder()
                .userId(1L)
                .cardHolder("John Doe")
                .initialBalance(new BigDecimal("500.00"))
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/user/cards/request/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(adminCardService, times(1)).requestCreateCardForUser(any(CardDtoIn.class));
    }

    @Test
    void requestCreateCard_WithMissingCardHolder_ShouldReturnBadRequest() throws Exception {
        CardDtoIn request = CardDtoIn.builder()
                .userId(1L)
                .cardHolder("") // Нарушает валидацию @NotBlank
                .initialBalance(new BigDecimal("500.00"))
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/user/cards/request/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(adminCardService, never()).requestCreateCardForUser(any());
    }

    @Test
    void requestForBlockCard_WithValidRequest_ShouldCallService() throws Exception {
        Long userId = 1L;

        CardDtoBlockRequest request = CardDtoBlockRequest.builder()
                .cardNumber("1234567812345678")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/user/cards/request/block")
                        .header("X-USER-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(adminCardService, times(1)).requestBlockCardForUser(userId, "1234567812345678");
    }

    @Test
    void requestForBlockCard_WithoutUserIdHeader_ShouldReturnBadRequest() throws Exception {
        CardDtoBlockRequest request = CardDtoBlockRequest.builder()
                .cardNumber("1234567812345678")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/user/cards/request/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(adminCardService, never()).requestBlockCardForUser(anyLong(), anyString());
    }

    @Test
    void getCardThroughCardIdForUser_WithValidData_ShouldReturnCard() throws Exception {
        Long userId = 1L;
        Long cardId = 100L;

        CardDtoOutUser expectedCard = CardDtoOutUser.builder()
                .cardHolder("John Doe")
                .expiryDate(LocalDate.now().plusYears(3))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1500.00"))
                .cardNumberMasked("**** **** **** 5678")
                .build();

        when(cardService.getCardThroughCardId(userId, cardId)).thenReturn(expectedCard);

        mockMvc.perform(get("/api/v1/user/cards/{cardId}", cardId)
                        .header("X-USER-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardHolder").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(1500.00))
                .andExpect(jsonPath("$.cardNumberMasked").value("**** **** **** 5678"));

        verify(cardService, times(1)).getCardThroughCardId(userId, cardId);
    }

    @Test
    void updateBalanceCard_WithValidRequest_ShouldReturnUpdatedCard() throws Exception {
        RequestUpdateBalance request = RequestUpdateBalance.builder()
                .cardNumber("1234567812345678")
                .cardHolder("John Doe")
                .addedAmount(new BigDecimal("500.00"))
                .build();

        CardDtoOutUser updatedCard = CardDtoOutUser.builder()
                .cardHolder("John Doe")
                .balance(new BigDecimal("2000.00"))
                .cardNumberMasked("**** **** **** 5678")
                .build();

        when(cardService.updateBalanceCard(any(RequestUpdateBalance.class))).thenReturn(updatedCard);

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/v1/user/cards/{cartId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardHolder").value("John Doe"))
                .andExpect(jsonPath("$.balance").value(2000.00));

        verify(cardService, times(1)).updateBalanceCard(any(RequestUpdateBalance.class));
    }

    @Test
    void updateBalanceCard_WithNegativeAmount_ShouldReturnBadRequest() throws Exception {
        RequestUpdateBalance request = RequestUpdateBalance.builder()
                .cardNumber("1234567812345678")
                .cardHolder("John Doe")
                .addedAmount(new BigDecimal("-100.00")) // Нарушает @PositiveOrZero
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/v1/user/cards/{cartId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).updateBalanceCard(any());
    }

    @Test
    void transferFromOneCardToAnother_WithValidRequest_ShouldCallService() throws Exception {
        TransferRequest transferRequest = TransferRequest.builder()
                .cardNumberFrom("1234567812345678")
                .cardHolderFrom("John Doe")
                .addedAmount(new BigDecimal("500.00"))
                .cardNumberTo("8765432187654321")
                .cardHolderTo("Jane Smith")
                .build();

        String requestBody = objectMapper.writeValueAsString(transferRequest);

        mockMvc.perform(patch("/api/v1/user/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(cardService, times(1)).transferFromOneCardToAnother(any(TransferRequest.class));
    }

    @Test
    void transferFromOneCardToAnother_WithMissingFields_ShouldReturnBadRequest() throws Exception {
        TransferRequest transferRequest = TransferRequest.builder()
                .cardNumberFrom("1234567812345678")
                .cardHolderFrom("") // Нарушает @NotBlank
                .addedAmount(new BigDecimal("500.00"))
                .cardNumberTo("8765432187654321")
                .cardHolderTo("Jane Smith")
                .build();

        String requestBody = objectMapper.writeValueAsString(transferRequest);

        mockMvc.perform(patch("/api/v1/user/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).transferFromOneCardToAnother(any());
    }

    @Test
    void transferFromOneCardToAnother_WithZeroAmount_ShouldCallService() throws Exception {
        TransferRequest transferRequest = TransferRequest.builder()
                .cardNumberFrom("1234567812345678")
                .cardHolderFrom("John Doe")
                .addedAmount(BigDecimal.ZERO)
                .cardNumberTo("8765432187654321")
                .cardHolderTo("Jane Smith")
                .build();

        String requestBody = objectMapper.writeValueAsString(transferRequest);

        mockMvc.perform(patch("/api/v1/user/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(cardService, times(1)).transferFromOneCardToAnother(any(TransferRequest.class));
    }

    @Test
    void transferFromOneCardToAnother_WithNullAmount_ShouldReturnBadRequest() throws Exception {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setCardNumberFrom("1234567812345678");
        transferRequest.setCardHolderFrom("John Doe");
        transferRequest.setAddedAmount(null); // Нарушает @NotNull
        transferRequest.setCardNumberTo("8765432187654321");
        transferRequest.setCardHolderTo("Jane Smith");

        String requestBody = objectMapper.writeValueAsString(transferRequest);

        mockMvc.perform(patch("/api/v1/user/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).transferFromOneCardToAnother(any());
    }

    @Test
    void updateBalanceCard_WithMissingCardHolder_ShouldReturnBadRequest() throws Exception {
        RequestUpdateBalance request = RequestUpdateBalance.builder()
                .cardNumber("1234567812345678")
                .cardHolder("") // Нарушает @NotBlank
                .addedAmount(new BigDecimal("500.00"))
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/v1/user/cards/{cartId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).updateBalanceCard(any());
    }

    @Test
    void requestForBlockCard_WithNullCardNumber_ShouldReturnBadRequest() throws Exception {
        Long userId = 1L;

        CardDtoBlockRequest request = new CardDtoBlockRequest();
        request.setCardNumber(null);

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/v1/user/cards/request/block")
                        .header("X-USER-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(adminCardService, never()).requestBlockCardForUser(anyLong(), anyString());
    }

    @Test
    void transferFromOneCardToAnother_ShouldPassCorrectDataToService() throws Exception {
        TransferRequest expectedRequest = TransferRequest.builder()
                .cardNumberFrom("1234567812345678")
                .cardHolderFrom("John Doe")
                .addedAmount(new BigDecimal("500.00"))
                .cardNumberTo("8765432187654321")
                .cardHolderTo("Jane Smith")
                .build();

        String requestBody = objectMapper.writeValueAsString(expectedRequest);

        mockMvc.perform(patch("/api/v1/user/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(cardService, times(1)).transferFromOneCardToAnother(argThat(request ->
                request.getCardNumberFrom().equals("1234567812345678") &&
                        request.getCardHolderFrom().equals("John Doe") &&
                        request.getAddedAmount().equals(new BigDecimal("500.00")) &&
                        request.getCardNumberTo().equals("8765432187654321") &&
                        request.getCardHolderTo().equals("Jane Smith")
        ));
    }

    @Test
    void updateBalanceCard_ShouldPassCorrectDataToService() throws Exception {
        RequestUpdateBalance expectedRequest = RequestUpdateBalance.builder()
                .cardNumber("1234567812345678")
                .cardHolder("John Doe")
                .addedAmount(new BigDecimal("300.00"))
                .build();

        CardDtoOutUser response = CardDtoOutUser.builder()
                .balance(new BigDecimal("1300.00"))
                .build();

        when(cardService.updateBalanceCard(any(RequestUpdateBalance.class))).thenReturn(response);

        String requestBody = objectMapper.writeValueAsString(expectedRequest);

        mockMvc.perform(patch("/api/v1/user/cards/{cartId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(cardService, times(1)).updateBalanceCard(argThat(request ->
                request.getCardNumber().equals("1234567812345678") &&
                        request.getCardHolder().equals("John Doe") &&
                        request.getAddedAmount().equals(new BigDecimal("300.00"))
        ));
    }

    @Test
    void requestCreateCard_WithNullUserId_ShouldReturnBadRequest() throws Exception {
        CardDtoIn request = CardDtoIn.builder()
                .userId(null)
                .cardHolder("John Doe")
                .initialBalance(new BigDecimal("500.00"))
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/user/cards/request/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(adminCardService, never()).requestCreateCardForUser(any());
    }

    @Test
    void updateBalanceCard_WithNullAmount_ShouldReturnBadRequest() throws Exception {
        RequestUpdateBalance request = new RequestUpdateBalance();
        request.setCardNumber("1234567812345678");
        request.setCardHolder("John Doe");
        request.setAddedAmount(null);

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/v1/user/cards/{cartId}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).updateBalanceCard(any());
    }


    @Test
    void requestCreateCard_WithZeroInitialBalance_ShouldBeValid() throws Exception {
        CardDtoIn request = CardDtoIn.builder()
                .userId(1L)
                .cardHolder("John Doe")
                .initialBalance(BigDecimal.ZERO)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/user/cards/request/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(adminCardService, times(1)).requestCreateCardForUser(any(CardDtoIn.class));
    }

}