package com.example.bankcards.service.card.admin;

import com.example.bankcards.dto.card.CardDtoBlock;
import com.example.bankcards.dto.card.CardDtoIn;
import com.example.bankcards.dto.card.CardDtoOut;
import com.example.bankcards.dto.enums.CardStatus;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.user.UserServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AdminCardServiceImpl implements AdminCardService {

    CardRepository cardRepository;
    UserServiceImpl userService;

    Map<Long, String> requestForBlockingCards = new HashMap<>();

    Set<CardDtoIn> requestsForCreateCard = new HashSet<>();

    @NonFinal
    @Value("${expiry.years}")
    Integer expiryYears;

    @Override
    @Transactional(readOnly = true)
    public List<CardDtoOut> getAllCards() {
        List<Card> cards = cardRepository.findAll();
        return CardMapper.toCardDtoOutList(cards);
    }

    @Override
    public void createCardForUser() {
        for (CardDtoIn cardDtoIn : requestsForCreateCard) {
            User userForWhomCard = userService.getEntityById(cardDtoIn.getUserId());
            Card saveCard = CardMapper.toCard(userForWhomCard, cardDtoIn);
            saveCard.setExpiryDate(LocalDate.now().plusYears(expiryYears));
            cardRepository.save(saveCard);
        }
        requestsForCreateCard.clear();
    }


    @Override
    public void requestBlockCardForUser(Long userId, String cardNumber) {
        findByUserIdAndCardNumber(userId, cardNumber);
        requestForBlockingCards.put(userId, cardNumber);
    }


    @Override
    public void blockCardForUsers() {
        for (Map.Entry<Long, String> entry : requestForBlockingCards.entrySet()) {
            Long userId = entry.getKey();
            String cardNumber = entry.getValue();

            Card card = findByUserIdAndCardNumber(userId, cardNumber);

            card.setStatus(CardStatus.BLOCKED);
            cardRepository.save(card);
        }
        requestForBlockingCards.clear();
    }

    @Override
    public void removeCardForUser(CardDtoBlock cardDtoBlock) {
        Card removeCard = findByUserIdAndCardNumber(cardDtoBlock.getUserId(), cardDtoBlock.getCardNumber());
        removeCard.setStatus(CardStatus.EXPIRED);
        cardRepository.save(removeCard);
    }

    @Override
    public void requestCreateCardForUser(CardDtoIn cardDtoIn) {
        requestsForCreateCard.add(cardDtoIn);
    }


    private Card findByUserIdAndCardNumber(Long userId, String cardNumber) {
        return cardRepository.findByUserIdAndCardNumber(userId, cardNumber)
                .orElseThrow(() -> new CardNotFoundException(String.format("Карта для пользователя с %d не найдена", userId)));
    }

}
