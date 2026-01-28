package com.example.bankcards.service.card.admin;

import com.example.bankcards.dto.card.CardDtoBlock;
import com.example.bankcards.dto.card.CardDtoIn;
import com.example.bankcards.dto.card.CardDtoOut;
import jakarta.validation.Valid;

import java.util.List;

public interface AdminCardService {

    List<CardDtoOut> getAllCards();

    CardDtoOut createCardForUser(CardDtoIn cardDtoIn);

    void blockCardForUsers();

    void requestBlockCardForUser(Long userId, String cardNumber);

    void removeCardForUser(@Valid CardDtoBlock cardDtoBlock);


}
