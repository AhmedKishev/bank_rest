package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByUserIdAndCardNumber(Long userId, String cardNumber);

    Page<Card> findAllByUserId(Long userId, Pageable pageable);

    Optional<Card> findByUserIdAndCardId(Long userId, Long cardId);

    Optional<Card> findByCardNumberAndCardHolder(String cardNumber, String cardHolder);
}
