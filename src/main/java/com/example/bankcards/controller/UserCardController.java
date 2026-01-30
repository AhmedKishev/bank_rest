package com.example.bankcards.controller;


import com.example.bankcards.dto.card.*;
import com.example.bankcards.service.card.admin.AdminCardService;
import com.example.bankcards.service.card.user.UserCardService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/cards")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Validated
public class UserCardController {

    UserCardService cardService;
    AdminCardService adminCardService;

    @GetMapping
    Page<CardDtoOutUser> getAllCardsForUser(@RequestHeader("X-USER-ID") Long userId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return cardService.getAllCardsForUser(userId, page, size);
    }

    @PostMapping("/request/create")
    public void requestCreateCard(@RequestBody @Valid CardDtoIn cardDtoIn) {
        adminCardService.requestCreateCardForUser(cardDtoIn);
    }

    @PutMapping("/request/block")
    public void requestForBlockCard(@RequestHeader("X-USER-ID") Long userId,
                                    @RequestBody @Valid CardDtoBlockRequest cardDtoBlockRequest) {
        adminCardService.requestBlockCardForUser(userId, cardDtoBlockRequest.getCardNumber());
    }

    @GetMapping("/{cardId}")
    public CardDtoOutUser getCardThroughCardIdForUser(@RequestHeader("X-USER-ID") Long userId,
                                                      @PathVariable Long cardId) {
        return cardService.getCardThroughCardId(userId, cardId);
    }

    @PatchMapping("/{cartId}")
    public CardDtoOutUser updateBalanceCard(@RequestBody @Valid RequestUpdateBalance requestUpdateBalance) {
        return cardService.updateBalanceCard(requestUpdateBalance);
    }

    @PatchMapping
    public void transferFromOneCardToAnother(@RequestBody @Valid TransferRequest transferRequest) {
        cardService.transferFromOneCardToAnother(transferRequest);
    }


}
