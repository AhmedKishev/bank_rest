package com.example.bankcards.service.card.user;

import com.example.bankcards.dto.card.CardDtoOutUser;
import com.example.bankcards.dto.card.RequestUpdateBalance;
import com.example.bankcards.dto.card.ToCardUpdateBalanceFromAnother;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

public interface UserCardService {

    Page<CardDtoOutUser> getAllCardsForUser(Long userId, int page, int size);

    CardDtoOutUser getCardThroughCardId(Long userId, Long cardId);

    CardDtoOutUser updateBalanceCard(RequestUpdateBalance requestUpdateBalance);

    void transferFromOneCardToAnother(@Valid RequestUpdateBalance fromCard, @Valid ToCardUpdateBalanceFromAnother toCard);
}
