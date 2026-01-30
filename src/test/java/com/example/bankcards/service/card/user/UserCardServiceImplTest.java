package com.example.bankcards.service.card.user;

import com.example.bankcards.dto.card.CardDtoOutUser;
import com.example.bankcards.dto.card.RequestUpdateBalance;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.dto.enums.CardStatus;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardDoesNotWorkException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.NotEnoughMoneyException;
import com.example.bankcards.repository.CardRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private UserCardServiceImpl userCardService;

    private User testUser;
    private Card testCard;
    private Card anotherCard;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .build();

        testCard = Card.builder()
                .cardId(1L)
                .cardNumber("1234567812345678")
                .cardNumberMasked("**** **** **** 5678")
                .cardHolder("John Doe")
                .expiryDate(LocalDate.now().plusYears(3))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .user(testUser)
                .build();

        anotherCard = Card.builder()
                .cardId(2L)
                .cardNumber("8765432187654321")
                .cardNumberMasked("**** **** **** 4321")
                .cardHolder("Jane Smith")
                .expiryDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("500.00"))
                .user(testUser)
                .build();
    }

    @Test
    void getAllCardsForUser_ShouldReturnPagedCards() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Card> cardPage = new PageImpl<>(List.of(testCard, anotherCard), pageable, 2);

        when(cardRepository.findAllByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(cardPage);

        Page<CardDtoOutUser> result = userCardService.getAllCardsForUser(1L, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(cardRepository, times(1)).findAllByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    void getAllCardsForUser_WithNoCards_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Card> emptyPage = Page.empty(pageable);

        when(cardRepository.findAllByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<CardDtoOutUser> result = userCardService.getAllCardsForUser(1L, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(cardRepository, times(1)).findAllByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    void getCardThroughCardId_WithValidCard_ShouldReturnCardDto() {
        when(cardRepository.findByUserIdAndCardId(1L, 1L))
                .thenReturn(Optional.of(testCard));

        CardDtoOutUser result = userCardService.getCardThroughCardId(1L, 1L);

        assertNotNull(result);
        assertEquals(testCard.getCardNumberMasked(), result.getCardNumberMasked());
        verify(cardRepository, times(1)).findByUserIdAndCardId(1L, 1L);
    }

    @Test
    void getCardThroughCardId_WithNonExistingCard_ShouldThrowCardNotFoundException() {
        when(cardRepository.findByUserIdAndCardId(1L, 999L))
                .thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> userCardService.getCardThroughCardId(1L, 999L)
        );

        assertTrue(exception.getMessage().contains("Карты с id 999 не существует"));
        verify(cardRepository, times(1)).findByUserIdAndCardId(1L, 999L);
    }

    @Test
    void getCardThroughCardId_WithWrongUser_ShouldThrowCardNotFoundException() {
        when(cardRepository.findByUserIdAndCardId(2L, 1L)) // другой пользователь
                .thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> userCardService.getCardThroughCardId(2L, 1L)
        );

        assertTrue(exception.getMessage().contains("Карты с id 1 не существует"));
        verify(cardRepository, times(1)).findByUserIdAndCardId(2L, 1L);
    }

    @Test
    @Transactional
    void updateBalanceCard_WithValidCard_ShouldUpdateBalance() {
        RequestUpdateBalance request = RequestUpdateBalance.builder()
                .cardNumber("1234567812345678")
                .cardHolder("John Doe")
                .addedAmount(new BigDecimal("500.00"))
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("1234567812345678", "John Doe"))
                .thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        CardDtoOutUser result = userCardService.updateBalanceCard(request);

        assertNotNull(result);
        verify(cardRepository, times(1)).findByCardNumberAndCardHolder("1234567812345678", "John Doe");
        verify(cardRepository, times(1)).save(argThat(card ->
                card.getBalance().compareTo(new BigDecimal("1500.00")) == 0
        ));
    }

    @Test
    @Transactional
    void updateBalanceCard_WithBlockedCard_ShouldThrowCardDoesNotWorkException() {
        testCard.setStatus(CardStatus.BLOCKED);
        RequestUpdateBalance request = RequestUpdateBalance.builder()
                .cardNumber("1234567812345678")
                .cardHolder("John Doe")
                .addedAmount(new BigDecimal("500.00"))
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("1234567812345678", "John Doe"))
                .thenReturn(Optional.of(testCard));

        CardDoesNotWorkException exception = assertThrows(
                CardDoesNotWorkException.class,
                () -> userCardService.updateBalanceCard(request)
        );

        assertTrue(exception.getMessage().contains("Карта пользователя John Doe не рабочая"));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @Transactional
    void updateBalanceCard_WithExpiredCard_ShouldThrowCardDoesNotWorkException() {
        testCard.setStatus(CardStatus.EXPIRED);
        RequestUpdateBalance request = RequestUpdateBalance.builder()
                .cardNumber("1234567812345678")
                .cardHolder("John Doe")
                .addedAmount(new BigDecimal("500.00"))
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("1234567812345678", "John Doe"))
                .thenReturn(Optional.of(testCard));

        CardDoesNotWorkException exception = assertThrows(
                CardDoesNotWorkException.class,
                () -> userCardService.updateBalanceCard(request)
        );

        assertTrue(exception.getMessage().contains("Карта пользователя John Doe не рабочая"));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @Transactional
    void updateBalanceCard_WithNonExistingCard_ShouldThrowCardNotFoundException() {
        RequestUpdateBalance request = RequestUpdateBalance.builder()
                .cardNumber("9999999999999999")
                .cardHolder("Non Existing")
                .addedAmount(new BigDecimal("500.00"))
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("9999999999999999", "Non Existing"))
                .thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> userCardService.updateBalanceCard(request)
        );

        assertTrue(exception.getMessage().contains("Карты с пользователем Non Existing не существует"));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @Transactional
    void updateBalanceCard_WithZeroAmount_ShouldUpdateBalance() {
        RequestUpdateBalance request = RequestUpdateBalance.builder()
                .cardNumber("1234567812345678")
                .cardHolder("John Doe")
                .addedAmount(BigDecimal.ZERO)
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("1234567812345678", "John Doe"))
                .thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        CardDtoOutUser result = userCardService.updateBalanceCard(request);

        assertNotNull(result);
        verify(cardRepository, times(1)).save(argThat(card ->
                card.getBalance().compareTo(new BigDecimal("1000.00")) == 0  // баланс не изменился
        ));
    }

    @Test
    @Transactional
    void updateBalanceCard_WithNegativeAmount_ShouldDecreaseBalance() {
        RequestUpdateBalance request = RequestUpdateBalance.builder()
                .cardNumber("1234567812345678")
                .cardHolder("John Doe")
                .addedAmount(new BigDecimal("-200.00"))
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("1234567812345678", "John Doe"))
                .thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        CardDtoOutUser result = userCardService.updateBalanceCard(request);

        assertNotNull(result);
        verify(cardRepository, times(1)).save(argThat(card ->
                card.getBalance().compareTo(new BigDecimal("800.00")) == 0
        ));
    }

    @Test
    @Transactional
    void transferFromOneCardToAnother_WithValidCards_ShouldTransferMoney() {
        TransferRequest request = TransferRequest.builder()
                .cardNumberFrom("1234567812345678")
                .cardHolderFrom("John Doe")
                .cardNumberTo("8765432187654321")
                .cardHolderTo("Jane Smith")
                .addedAmount(new BigDecimal("300.00"))
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("1234567812345678", "John Doe"))
                .thenReturn(Optional.of(testCard));
        when(cardRepository.findByCardNumberAndCardHolder("8765432187654321", "Jane Smith"))
                .thenReturn(Optional.of(anotherCard));

        Card updatedFromCard = Card.builder()
                .cardId(1L)
                .cardNumber("1234567812345678")
                .cardHolder("John Doe")
                .balance(new BigDecimal("700.00"))
                .status(CardStatus.ACTIVE)
                .build();

        Card updatedToCard = Card.builder()
                .cardId(2L)
                .cardNumber("8765432187654321")
                .cardHolder("Jane Smith")
                .balance(new BigDecimal("800.00"))
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.saveAll(any(Iterable.class))).thenReturn(List.of(updatedFromCard, updatedToCard));

        userCardService.transferFromOneCardToAnother(request);

        verify(cardRepository, times(1)).findByCardNumberAndCardHolder("1234567812345678", "John Doe");
        verify(cardRepository, times(1)).findByCardNumberAndCardHolder("8765432187654321", "Jane Smith");
        verify(cardRepository, times(1)).saveAll(any(Iterable.class));
    }

    @Test
    @Transactional
    void transferFromOneCardToAnother_WithInsufficientFunds_ShouldThrowNotEnoughMoneyException() {
        // Arrange
        TransferRequest request = TransferRequest.builder()
                .cardNumberFrom("1234567812345678")
                .cardHolderFrom("John Doe")
                .cardNumberTo("8765432187654321")
                .cardHolderTo("Jane Smith")
                .addedAmount(new BigDecimal("1500.00")) // больше чем на карте
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("1234567812345678", "John Doe"))
                .thenReturn(Optional.of(testCard));

        // Act & Assert
        NotEnoughMoneyException exception = assertThrows(
                NotEnoughMoneyException.class,
                () -> userCardService.transferFromOneCardToAnother(request)
        );

        assertTrue(exception.getMessage().contains("В карте пользователя John Doe недостаточно средств для перевода"));
        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    @Transactional
    void transferFromOneCardToAnother_WithBlockedSourceCard_ShouldThrowCardDoesNotWorkException() {
        testCard.setStatus(CardStatus.BLOCKED);
        TransferRequest request = TransferRequest.builder()
                .cardNumberFrom("1234567812345678")
                .cardHolderFrom("John Doe")
                .cardNumberTo("8765432187654321")
                .cardHolderTo("Jane Smith")
                .addedAmount(new BigDecimal("300.00"))
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("1234567812345678", "John Doe"))
                .thenReturn(Optional.of(testCard));

        CardDoesNotWorkException exception = assertThrows(
                CardDoesNotWorkException.class,
                () -> userCardService.transferFromOneCardToAnother(request)
        );

        assertTrue(exception.getMessage().contains("Карта пользователя John Doe не рабочая"));
        verify(cardRepository, never()).saveAll(any(Iterable.class));
    }

    @Test
    @Transactional
    void transferFromOneCardToAnother_WithNonExistingSourceCard_ShouldThrowCardNotFoundException() {
        // Arrange
        TransferRequest request = TransferRequest.builder()
                .cardNumberFrom("9999999999999999")
                .cardHolderFrom("Non Existing")
                .cardNumberTo("8765432187654321")
                .cardHolderTo("Jane Smith")
                .addedAmount(new BigDecimal("300.00"))
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("9999999999999999", "Non Existing"))
                .thenReturn(Optional.empty());

        // Act & Assert
        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> userCardService.transferFromOneCardToAnother(request)
        );

        assertTrue(exception.getMessage().contains("Карты с пользователем Non Existing не существует"));
        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    @Transactional
    void transferFromOneCardToAnother_WithNonExistingTargetCard_ShouldThrowCardNotFoundException() {
        // Arrange
        TransferRequest request = TransferRequest.builder()
                .cardNumberFrom("1234567812345678")
                .cardHolderFrom("John Doe")
                .cardNumberTo("9999999999999999")
                .cardHolderTo("Non Existing")
                .addedAmount(new BigDecimal("300.00"))
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("1234567812345678", "John Doe"))
                .thenReturn(Optional.of(testCard));
        when(cardRepository.findByCardNumberAndCardHolder("9999999999999999", "Non Existing"))
                .thenReturn(Optional.empty());

        // Act & Assert
        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> userCardService.transferFromOneCardToAnother(request)
        );

        assertTrue(exception.getMessage().contains("Карты с пользователем Non Existing не существует"));
        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    @Transactional
    void transferFromOneCardToAnother_WithExactAmount_ShouldTransferSuccessfully() {
        TransferRequest request = TransferRequest.builder()
                .cardNumberFrom("1234567812345678")
                .cardHolderFrom("John Doe")
                .cardNumberTo("8765432187654321")
                .cardHolderTo("Jane Smith")
                .addedAmount(new BigDecimal("1000.00"))
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("1234567812345678", "John Doe"))
                .thenReturn(Optional.of(testCard));
        when(cardRepository.findByCardNumberAndCardHolder("8765432187654321", "Jane Smith"))
                .thenReturn(Optional.of(anotherCard));

        ArgumentCaptor<Iterable<Card>> captor = ArgumentCaptor.forClass(Iterable.class);
        when(cardRepository.saveAll(captor.capture())).thenReturn(List.of(testCard, anotherCard));

        userCardService.transferFromOneCardToAnother(request);

        List<Card> savedCards = new ArrayList<>();
        captor.getValue().forEach(savedCards::add);

        assertEquals(2, savedCards.size());
        assertEquals(0, savedCards.get(0).getBalance().compareTo(BigDecimal.ZERO));
        assertEquals(0, savedCards.get(1).getBalance().compareTo(new BigDecimal("1500.00")));
    }

    @Test
    @Transactional
    void transferFromOneCardToAnother_WithZeroAmount_ShouldDoNothing() {
        TransferRequest request = TransferRequest.builder()
                .cardNumberFrom("1234567812345678")
                .cardHolderFrom("John Doe")
                .cardNumberTo("8765432187654321")
                .cardHolderTo("Jane Smith")
                .addedAmount(BigDecimal.ZERO)
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("1234567812345678", "John Doe"))
                .thenReturn(Optional.of(testCard));
        when(cardRepository.findByCardNumberAndCardHolder("8765432187654321", "Jane Smith"))
                .thenReturn(Optional.of(anotherCard));

        ArgumentCaptor<Iterable<Card>> captor = ArgumentCaptor.forClass(Iterable.class);
        when(cardRepository.saveAll(captor.capture())).thenReturn(List.of(testCard, anotherCard));

        userCardService.transferFromOneCardToAnother(request);

        List<Card> savedCards = new ArrayList<>();
        captor.getValue().forEach(savedCards::add);

        assertEquals(2, savedCards.size());
        assertEquals(0, savedCards.get(0).getBalance().compareTo(new BigDecimal("1000.00")));
        assertEquals(0, savedCards.get(1).getBalance().compareTo(new BigDecimal("500.00")));
    }

    @Test
    @Transactional
    void transferFromOneCardToAnother_WithSameCard_ShouldWork() {
        TransferRequest request = TransferRequest.builder()
                .cardNumberFrom("1234567812345678")
                .cardHolderFrom("John Doe")
                .cardNumberTo("1234567812345678")
                .cardHolderTo("John Doe")
                .addedAmount(new BigDecimal("300.00"))
                .build();

        when(cardRepository.findByCardNumberAndCardHolder("1234567812345678", "John Doe"))
                .thenReturn(Optional.of(testCard))
                .thenReturn(Optional.of(testCard));

        ArgumentCaptor<Iterable<Card>> captor = ArgumentCaptor.forClass(Iterable.class);
        when(cardRepository.saveAll(captor.capture())).thenReturn(List.of(testCard, testCard));

        userCardService.transferFromOneCardToAnother(request);

        verify(cardRepository, times(2)).findByCardNumberAndCardHolder("1234567812345678", "John Doe");

        List<Card> savedCards = new ArrayList<>();
        captor.getValue().forEach(savedCards::add);

        assertEquals(2, savedCards.size());
        assertEquals(0, savedCards.get(0).getBalance().compareTo(new BigDecimal("1000.00")));
        assertEquals(0, savedCards.get(1).getBalance().compareTo(new BigDecimal("1000.00")));
    }
}