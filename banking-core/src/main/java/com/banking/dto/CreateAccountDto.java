package com.banking.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.banking.model.AccountType;

/**
 * DTO for creating a new account.
 * Used to transfer account creation request data from client to service.
 */
public record CreateAccountDto(
        String ownerId,
        String currency,
        BigDecimal initialBalance,
        AccountType accountType,
        Instant createdAt,
        Instant updatedAt
) {
    // Validation to ensure timestamps are consistent
    public CreateAccountDto {
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("Timestamps cannot be null");
        }
        if (createdAt.isAfter(updatedAt)) {
            throw new IllegalArgumentException("Created time cannot be after updated time");
        }
    }
}
