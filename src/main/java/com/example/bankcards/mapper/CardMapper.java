package com.example.bankcards.mapper;

import com.example.bankcards.dto.card.CardDtoIn;
import com.example.bankcards.dto.card.CardDtoOut;
import com.example.bankcards.dto.enums.CardStatus;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.util.CardEncryptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CardMapper {

    static CardEncryptionService encryptionService;

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
        return Card.builder()
                .cardHolder(cardDtoIn.getCardHolder())
                .user(user)
                .cardNumber(encryptionService.generateCardNumber())
                .status(CardStatus.ACTIVE)
                .build();
    }


}
