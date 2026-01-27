package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardDtoOut;
import com.example.bankcards.entity.Card;

import java.util.List;

public class CardMapper {

    public static List<CardDtoOut> toCardDtoOutList(List<Card> cards) {
        return cards.stream().map(CardMapper::toCardDtoOut).toList();
    }

    private static CardDtoOut toCardDtoOut(Card card) {
        return CardDtoOut.builder()
                .cardStatus(card.getStatus())
                .userDtoOut(UserMapper.toUserDtoOut(card.getUser()))
                .balance(card.getBalance())
                .expiryDate(card.getExpiryDate())
                .build();
    }


}
