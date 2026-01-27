package com.example.bankcards.service;

import com.example.bankcards.dto.CardDtoOut;
import com.example.bankcards.dto.UserDtoIn;
import com.example.bankcards.entity.Card;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    CardRepository cardRepository;
    UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<CardDtoOut> getAllCards() {
        List<Card> cards = cardRepository.findAll();
        return CardMapper.toCardDtoOutList(cards);
    }

    @Override
    public CardDtoOut createCardForUser(UserDtoIn userDtoIn) {
        Card card = new Card();

    }


}
