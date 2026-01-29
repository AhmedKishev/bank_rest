package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ToCardUpdateBalanceFromAnother {

    @NotBlank
    String cardNumber;

    @NotBlank
    String cardHolder;

}
