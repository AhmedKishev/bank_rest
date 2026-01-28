package com.example.bankcards.util;


import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CardEncryptionService {

    Random random = new Random();

    public String generateCardNumber() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(16));
        }

        return sb.toString();
    }

    public String maskCardNumber(String cardNumber) {
        return String.format("**** **** **** %s",
                cardNumber.substring(12, 16));
    }

}
