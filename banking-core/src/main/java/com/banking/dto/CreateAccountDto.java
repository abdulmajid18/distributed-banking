package com.banking.dto;

import java.math.BigDecimal;
import com.banking.model.AccountType;

/**
 * DTO for creating a new account.
 * Used to transfer account creation request data from client to service.
 */
public record CreateAccountDto(String ownerId, String currency, BigDecimal initialBalance, AccountType accountType) {

}
