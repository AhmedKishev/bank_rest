package com.example.bankcards.dto.card;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@Builder
public class RequestUpdateBalance {

    @NotBlank
    String cardNumber;

    @NotBlank
    String cardHolder;

    @PositiveOrZero
    @NotNull
    BigDecimal addedAmount;

}