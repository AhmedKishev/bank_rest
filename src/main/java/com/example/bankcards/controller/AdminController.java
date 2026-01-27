package com.example.bankcards.controller;


import com.example.bankcards.dto.CardDtoOut;
import com.example.bankcards.dto.UserDtoIn;
import com.example.bankcards.service.CardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/admin")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Validated
public class AdminController {

    CardService cardService;

    @GetMapping("/get-all-cards")
    List<CardDtoOut> getAllCards() {
        return cardService.getAllCards();
    }

    @PatchMapping("/create")
    public CardDtoOut createCardForUser(UserDtoIn userDtoIn) {
        return cardService.createCardForUser(userDtoIn);
    }

}
