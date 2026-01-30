package com.example.bankcards.dto.card;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CardDtoBlock {


    @NotNull
    Long userId;

    @NotBlank
    String cardNumber;

}
