package com.example.bankcards.entity;

import com.example.bankcards.dto.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "card_number", nullable = false)
    String cardNumber;

    @Column(name = )

    @Column(name = "card_holder", nullable = false)
    String cardHolder;

    @Column(name = "expiry_date", nullable = false)
    LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    CardStatus status;

    @Column(precision = 15, scale = 2)
    BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @CreationTimestamp
    LocalDateTime createdAt;

    @PreUpdate
    @PrePersist
    public void checkExpiry() {
        if (expiryDate.isBefore(LocalDate.now())) {
            this.status = CardStatus.EXPIRED;
        }
    }

}
