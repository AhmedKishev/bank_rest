package com.example.bankcards.controller;


import com.example.bankcards.dto.card.CardDtoBlock;
import com.example.bankcards.dto.card.CardDtoIn;
import com.example.bankcards.dto.card.CardDtoOut;
import com.example.bankcards.service.card.admin.AdminCardService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Validated
public class AdminController {

    AdminCardService cardService;

    @GetMapping("/cards")
    List<CardDtoOut> getAllCards() {
        return cardService.getAllCards();
    }

    @PatchMapping("/create")
    public CardDtoOut createCardForUser(@Valid @RequestBody CardDtoIn cardDtoIn) {
        return cardService.createCardForUser(cardDtoIn);
    }

    @PatchMapping("/block")
    public void blockCardForUsers() {
        cardService.blockCardForUsers();
    }

    @PatchMapping("/remove")
    public void removeCardForUser(@Valid @RequestBody CardDtoBlock cardDtoBlock) {
        cardService.removeCardForUser(cardDtoBlock);
    }

}
