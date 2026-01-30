package com.example.bankcards.service.card.admin;

import com.example.bankcards.dto.card.CardDtoBlock;
import com.example.bankcards.dto.card.CardDtoIn;
import com.example.bankcards.dto.card.CardDtoOut;
import com.example.bankcards.dto.enums.CardStatus;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.user.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private AdminCardServiceImpl adminCardService;

    private User testUser;
    private Card testCard;
    private CardDtoIn testCardDtoIn;
    private CardDtoBlock testCardDtoBlock;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminCardService, "expiryYears", 3);

        LocalDate expiryDate = LocalDate.now().plusYears(3);

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
                .expiryDate(expiryDate)
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .user(testUser)
                .build();

        testCardDtoIn = CardDtoIn.builder()
                .userId(1L)
                .cardHolder("John Doe")
                .initialBalance(new BigDecimal("500.00"))
                .build();

        testCardDtoBlock = CardDtoBlock.builder()
                .userId(1L)
                .cardNumber("1234567812345678")
                .build();

        clearInternalCollections();
    }

    private void clearInternalCollections() {
        ReflectionTestUtils.setField(adminCardService, "requestForBlockingCards", new HashMap<Long, String>());
        ReflectionTestUtils.setField(adminCardService, "requestsForCreateCard", new HashSet<>());
    }

    @Test
    void getAllCards_ShouldReturnListOfCardDtoOut() {
        List<Card> cards = Collections.singletonList(testCard);
        when(cardRepository.findAll()).thenReturn(cards);

        List<CardDtoOut> result = adminCardService.getAllCards();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository, times(1)).findAll();
    }

    @Test
    void getAllCards_WhenNoCards_ShouldReturnEmptyList() {
        when(cardRepository.findAll()).thenReturn(Collections.emptyList());

        List<CardDtoOut> result = adminCardService.getAllCards();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cardRepository, times(1)).findAll();
    }

    @Test
    void requestCreateCardForUser_ShouldAddToPendingRequests() {
        adminCardService.requestCreateCardForUser(testCardDtoIn);

        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        adminCardService.createCardForUser();


        verify(userService, times(1)).getEntityById(1L);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void createCardForUser_WithPendingRequest_ShouldCreateCard() {
        adminCardService.requestCreateCardForUser(testCardDtoIn);
        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        adminCardService.createCardForUser();


        verify(userService, times(1)).getEntityById(1L);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void createCardForUser_WithNoRequests_ShouldDoNothing() {
        adminCardService.createCardForUser();

        verify(userService, never()).getEntityById(anyLong());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void createCardForUser_WithMultipleRequests_ShouldCreateAllCards() {
        CardDtoIn secondCardDtoIn = CardDtoIn.builder()
                .userId(2L)
                .cardHolder("Jane Smith")
                .initialBalance(new BigDecimal("1000.00"))
                .build();

        User secondUser = User.builder()
                .id(2L)
                .username("jane_smith")
                .email("jane@example.com")
                .build();

        adminCardService.requestCreateCardForUser(testCardDtoIn);
        adminCardService.requestCreateCardForUser(secondCardDtoIn);

        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(userService.getEntityById(2L)).thenReturn(secondUser);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        adminCardService.createCardForUser();

        verify(userService, times(1)).getEntityById(1L);
        verify(userService, times(1)).getEntityById(2L);
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void requestBlockCardForUser_WithValidCard_ShouldAllowBlocking() {
        when(cardRepository.findByUserIdAndCardNumber(1L, "1234567812345678"))
                .thenReturn(Optional.of(testCard));

        adminCardService.requestBlockCardForUser(1L, "1234567812345678");

        Card cardToSave = Card.builder()
                .cardId(1L)
                .cardNumber("1234567812345678")
                .status(CardStatus.BLOCKED)
                .build();
        when(cardRepository.save(any(Card.class))).thenReturn(cardToSave);

        adminCardService.blockCardForUsers();

        verify(cardRepository, times(2)).findByUserIdAndCardNumber(1L, "1234567812345678");
        verify(cardRepository, times(1)).save(argThat(card ->
                card.getStatus() == CardStatus.BLOCKED
        ));
    }

    @Test
    void requestBlockCardForUser_WithNonExistingCard_ShouldThrowException() {
        when(cardRepository.findByUserIdAndCardNumber(1L, "9999999999999999"))
                .thenReturn(Optional.empty());


        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> adminCardService.requestBlockCardForUser(1L, "9999999999999999")
        );

        assertTrue(exception.getMessage().contains("Карта для пользователя с 1 не найдена"));
    }

    @Test
    void blockCardForUsers_WithNoRequests_ShouldDoNothing() {
        adminCardService.blockCardForUsers();

        verify(cardRepository, never()).findByUserIdAndCardNumber(anyLong(), anyString());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void blockCardForUsers_AfterRequest_ShouldBlockCard() {
        when(cardRepository.findByUserIdAndCardNumber(1L, "1234567812345678"))
                .thenReturn(Optional.of(testCard));

        adminCardService.requestBlockCardForUser(1L, "1234567812345678");

        Card blockedCard = Card.builder()
                .cardId(1L)
                .cardNumber("1234567812345678")
                .status(CardStatus.BLOCKED)
                .build();

        when(cardRepository.findByUserIdAndCardNumber(1L, "1234567812345678"))
                .thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(blockedCard);

        adminCardService.blockCardForUsers();

        verify(cardRepository, times(2)).findByUserIdAndCardNumber(1L, "1234567812345678");
        verify(cardRepository, times(1)).save(argThat(card ->
                card.getStatus() == CardStatus.BLOCKED
        ));
    }

    @Test
    void blockCardForUsers_WithMultipleRequests_ShouldBlockAllCards() {
        User secondUser = User.builder()
                .id(2L)
                .username("jane_smith")
                .email("jane@example.com")
                .build();

        Card secondCard = Card.builder()
                .cardId(2L)
                .cardNumber("8765432187654321")
                .cardHolder("Jane Smith")
                .status(CardStatus.ACTIVE)
                .user(secondUser)
                .build();

        when(cardRepository.findByUserIdAndCardNumber(1L, "1234567812345678"))
                .thenReturn(Optional.of(testCard))
                .thenReturn(Optional.of(testCard));

        when(cardRepository.findByUserIdAndCardNumber(2L, "8765432187654321"))
                .thenReturn(Optional.of(secondCard))
                .thenReturn(Optional.of(secondCard));

        adminCardService.requestBlockCardForUser(1L, "1234567812345678");
        adminCardService.requestBlockCardForUser(2L, "8765432187654321");

        when(cardRepository.save(any(Card.class))).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        adminCardService.blockCardForUsers();

        verify(cardRepository, times(2)).findByUserIdAndCardNumber(1L, "1234567812345678");
        verify(cardRepository, times(2)).findByUserIdAndCardNumber(2L, "8765432187654321");
        verify(cardRepository, times(2)).save(any(Card.class));
    }
    @Test
    void removeCardForUser_WithValidCard_ShouldMarkAsExpired() {
        when(cardRepository.findByUserIdAndCardNumber(1L, "1234567812345678"))
                .thenReturn(Optional.of(testCard));

        adminCardService.removeCardForUser(testCardDtoBlock);

        verify(cardRepository, times(1)).findByUserIdAndCardNumber(1L, "1234567812345678");
        verify(cardRepository, times(1)).save(argThat(card ->
                card.getStatus() == CardStatus.EXPIRED
        ));
    }

    @Test
    void removeCardForUser_WithNonExistingCard_ShouldThrowException() {
        when(cardRepository.findByUserIdAndCardNumber(1L, "9999999999999999"))
                .thenReturn(Optional.empty());

        CardDtoBlock invalidDto = CardDtoBlock.builder()
                .userId(1L)
                .cardNumber("9999999999999999")
                .build();

        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> adminCardService.removeCardForUser(invalidDto)
        );

        assertTrue(exception.getMessage().contains("Карта для пользователя с 1 не найдена"));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void removeCardForUser_WithBlockedCard_ShouldChangeToExpired() {
        testCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByUserIdAndCardNumber(1L, "1234567812345678"))
                .thenReturn(Optional.of(testCard));

        adminCardService.removeCardForUser(testCardDtoBlock);

        verify(cardRepository, times(1)).save(argThat(card ->
                card.getStatus() == CardStatus.EXPIRED
        ));
    }

    @Test
    void createCardForUser_ShouldSetExpiryDateCorrectly() {
        adminCardService.requestCreateCardForUser(testCardDtoIn);

        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card savedCard = invocation.getArgument(0);
            assertNotNull(savedCard.getExpiryDate());
            assertEquals(LocalDate.now().plusYears(3), savedCard.getExpiryDate());
            return savedCard;
        });

        adminCardService.createCardForUser();

        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void createCardForUser_ShouldSetActiveStatus() {
        adminCardService.requestCreateCardForUser(testCardDtoIn);

        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card savedCard = invocation.getArgument(0);
            assertEquals(CardStatus.ACTIVE, savedCard.getStatus());
            return savedCard;
        });

        adminCardService.createCardForUser();

        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void createCardForUser_ShouldGenerateCardNumberAndMask() {
        adminCardService.requestCreateCardForUser(testCardDtoIn);

        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card savedCard = invocation.getArgument(0);
            assertNotNull(savedCard.getCardNumber());
            assertNotNull(savedCard.getCardNumberMasked());
            assertTrue(savedCard.getCardNumberMasked().contains("****"));
            return savedCard;
        });

        adminCardService.createCardForUser();

        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void requestBlockCardForUser_WithNullUserId_ShouldThrowCardNotFoundException() {
        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> adminCardService.requestBlockCardForUser(null, "1234567812345678")
        );
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    void requestBlockCardForUser_WithNullCardNumber_ShouldThrowCardNotFoundException() {
        CardNotFoundException exception = assertThrows(
                CardNotFoundException.class,
                () -> adminCardService.requestBlockCardForUser(1L, null)
        );
        assertTrue(exception.getMessage().contains("1"));
    }

    @Test
    void removeCardForUser_WithNullDto_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> adminCardService.removeCardForUser(null));
    }


    @Test
    void createCardForUser_WithZeroBalance_ShouldCreateCard() {
        // Arrange
        CardDtoIn zeroBalanceCard = CardDtoIn.builder()
                .userId(1L)
                .cardHolder("John Doe")
                .initialBalance(BigDecimal.ZERO)
                .build();

        adminCardService.requestCreateCardForUser(zeroBalanceCard);

        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card savedCard = invocation.getArgument(0);
            assertEquals(BigDecimal.ZERO, savedCard.getBalance());
            return savedCard;
        });

        // Act
        adminCardService.createCardForUser();

        // Assert
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void createCardForUser_ShouldClearRequestsAfterProcessing() {
        clearInternalCollections();

        adminCardService.requestCreateCardForUser(testCardDtoIn);

        when(userService.getEntityById(1L)).thenReturn(testUser);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        adminCardService.createCardForUser();

        adminCardService.createCardForUser();

        verify(userService, times(1)).getEntityById(1L);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void blockCardForUsers_ShouldClearRequestsAfterProcessing() {
        clearInternalCollections();

        when(cardRepository.findByUserIdAndCardNumber(1L, "1234567812345678"))
                .thenReturn(Optional.of(testCard));
        adminCardService.requestBlockCardForUser(1L, "1234567812345678");

        Card blockedCard = Card.builder()
                .cardId(1L)
                .cardNumber("1234567812345678")
                .status(CardStatus.BLOCKED)
                .build();

        when(cardRepository.findByUserIdAndCardNumber(1L, "1234567812345678"))
                .thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(blockedCard);

        adminCardService.blockCardForUsers();

        adminCardService.blockCardForUsers();

        verify(cardRepository, times(2)).findByUserIdAndCardNumber(1L, "1234567812345678");
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void expiryYears_ShouldBeSet() {
        // Act & Assert
        Integer expiryYears = (Integer) ReflectionTestUtils.getField(adminCardService, "expiryYears");
        assertEquals(3, expiryYears);
    }
}