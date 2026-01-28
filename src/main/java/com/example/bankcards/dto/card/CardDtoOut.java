package com.example.bankcards.dto.card;

import com.example.bankcards.dto.user.UserDtoOut;
import com.example.bankcards.dto.enums.CardStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardDtoOut {


    UserDtoOut userDtoOut;

    LocalDate expiryDate;

    CardStatus cardStatus;

    BigDecimal balance;

}

