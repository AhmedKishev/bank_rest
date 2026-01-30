package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
public class TransferRequest {

    @NotBlank
    String cardNumberFrom;

    @NotBlank
    String cardHolderFrom;

    @PositiveOrZero
    @NotNull
    BigDecimal addedAmount;

    @NotBlank
    String cardNumberTo;

    @NotBlank
    String cardHolderTo;

}
