package com.example.bankcards.service.card.user;

import com.example.bankcards.dto.card.CardDtoOutUser;
import com.example.bankcards.dto.card.RequestUpdateBalance;
import com.example.bankcards.dto.card.TransferRequest;
import com.example.bankcards.dto.enums.CardStatus;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.CardDoesNotWorkException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.NotEnoughMoneyException;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserCardServiceImpl implements UserCardService {

    CardRepository cardRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CardDtoOutUser> getAllCardsForUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Card> cards;


        cards = cardRepository.findAllByUserId(
                userId,
                pageable
        );

        return cards.map(CardMapper::toCardDtoOutUser);
    }

    @Override
    @Transactional(readOnly = true)
    public CardDtoOutUser getCardThroughCardId(Long userId, Long cardId) {
        return CardMapper.toCardDtoOutUser(cardRepository.findByUserIdAndCardId(userId, cardId)
                .orElseThrow(() -> new CardNotFoundException(String.format("Карты с id %d не существует", cardId))));
    }

    @Override
    @Transactional
    public CardDtoOutUser updateBalanceCard(RequestUpdateBalance requestUpdateBalance) {
        Card updateCard = getCardByCardNumberAndCardHolder(requestUpdateBalance.getCardNumber(), requestUpdateBalance.getCardHolder());

        if (updateCard.getStatus() == CardStatus.BLOCKED || updateCard.getStatus() == CardStatus.EXPIRED) {
            throw new CardDoesNotWorkException(String.format("Карта пользователя %s не рабочая", requestUpdateBalance.getCardHolder()));
        }

        updateCard.setBalance(updateCard.getBalance().add(requestUpdateBalance.getAddedAmount()));
        cardRepository.save(updateCard);
        return CardMapper.toCardDtoOutUser(updateCard);
    }

    @Override
    @Transactional
    public void transferFromOneCardToAnother(TransferRequest transferRequest) {
        Card from = getCardByCardNumberAndCardHolder(transferRequest.getCardNumberFrom(), transferRequest.getCardHolderFrom());

        if (from.getStatus() == CardStatus.BLOCKED || from.getStatus() == CardStatus.EXPIRED) {
            throw new CardDoesNotWorkException(String.format("Карта пользователя %s не рабочая", from.getCardHolder()));
        }

        if (transferRequest.getAddedAmount().compareTo(from.getBalance()) > 0) {
            throw new NotEnoughMoneyException(String.format("В карте пользователя %s недостаточно средств для перевода", from.getCardHolder()));
        }

        from.setBalance(from.getBalance().subtract(transferRequest.getAddedAmount()));

        Card to = getCardByCardNumberAndCardHolder(transferRequest.getCardNumberTo(), transferRequest.getCardHolderTo());

        if (to.getStatus() == CardStatus.BLOCKED || to.getStatus() == CardStatus.EXPIRED) {
            throw new CardDoesNotWorkException(String.format("Карта пользователя %s не рабочая", to.getCardHolder()));
        }

        to.setBalance(to.getBalance().add(transferRequest.getAddedAmount()));

        cardRepository.saveAll(List.of(from, to));
    }

    private Card getCardByCardNumberAndCardHolder(String cardNumber, String cardHolder) {
        return cardRepository.findByCardNumberAndCardHolder(cardNumber, cardHolder)
                .orElseThrow(() -> new CardNotFoundException(String.format("Карты с пользователем %s не существует", cardHolder)));
    }


}
