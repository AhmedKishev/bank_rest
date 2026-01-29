package com.example.bankcards.mapper;

import com.example.bankcards.dto.card.CardDtoIn;
import com.example.bankcards.dto.card.CardDtoOut;
import com.example.bankcards.dto.card.CardDtoOutUser;
import com.example.bankcards.dto.enums.CardStatus;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.util.CardEncryptionService;

import java.math.BigDecimal;
import java.util.List;


public class CardMapper {


    public static List<CardDtoOut> toCardDtoOutList(List<Card> cards) {
        return cards.stream().map(CardMapper::toCardDtoOut).toList();
    }

    public static CardDtoOut toCardDtoOut(Card card) {
        return CardDtoOut.builder()
                .cardStatus(card.getStatus())
                .userDtoOut(UserMapper.toUserDtoOut(card.getUser()))
                .balance(card.getBalance())
                .expiryDate(card.getExpiryDate())
                .build();
    }

    public static Card toCard(User user, CardDtoIn cardDtoIn) {
        String cardNumber = CardEncryptionService.generateCardNumber();
        return Card.builder()
                .cardHolder(cardDtoIn.getCardHolder())
                .user(user)
                .cardNumber(cardNumber)
                .cardNumberMasked(CardEncryptionService.maskCardNumber(cardNumber))
                .status(CardStatus.ACTIVE)
                .balance(cardDtoIn.getInitialBalance() != null
                        ? cardDtoIn.getInitialBalance()
                        : BigDecimal.ZERO)
                .build();
    }


    public static CardDtoOutUser toCardDtoOutUser(Card card) {
        return CardDtoOutUser.builder()
                .cardNumberMasked(card.getCardNumberMasked())
                .cardHolder(card.getCardHolder())
                .status(card.getStatus())
                .balance(card.getBalance())
                .expiryDate(card.getExpiryDate())
                .build();
    }
}
