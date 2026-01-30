package com.example.bankcards.dto.card;

import com.example.bankcards.dto.enums.CardStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CardDtoOutUser {

    String cardHolder;

    LocalDate expiryDate;

    CardStatus status;

    BigDecimal balance;

    String cardNumberMasked;

}
