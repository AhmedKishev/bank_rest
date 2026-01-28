package com.example.bankcards.service.card.user;

import com.example.bankcards.dto.card.CardDtoOut;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserCardServiceImpl implements UserCardService {

    CardRepository cardRepository;

    @Override
    public Page<CardDtoOut> getAllCardsForUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Card> cards;


        cards = cardRepository.findByUserId(
                userId,
                pageable
        );

        return cards.map(CardMapper::toCardDtoOut);
    }

    @Override
    public CardDtoOut getCardThroughCardId(Long cardId) {
        return CardMapper.toCardDtoOut(cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(String.format("Карты с id %d не существует", cardId))));
    }

}
