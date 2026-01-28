package com.example.bankcards.service.card.user;

import com.example.bankcards.dto.card.CardDtoOut;
import org.springframework.data.domain.Page;

public interface UserCardService {

    Page<CardDtoOut> getAllCardsForUser(Long userId, int page, int size);

    CardDtoOut getCardThroughCardId(Long cardId);
}
