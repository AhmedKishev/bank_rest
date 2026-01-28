package com.example.bankcards.dto.card;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Setter
@Getter
public class CardDtoBlock {


    @NotNull
    Long userId;

    @NotBlank
    String cardNumber;

}
