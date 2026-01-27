package com.example.bankcards.service;

import com.example.bankcards.dto.CardDtoOut;
import com.example.bankcards.dto.UserDtoIn;

import java.util.List;

public interface CardService {

    List<CardDtoOut> getAllCards ();

    CardDtoOut createCardForUser(UserDtoIn userDtoIn);
}
