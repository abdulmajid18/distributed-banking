package com.banking.events.model;

import com.banking.events.EventType;
import com.banking.model.AccountType;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountCreationEvent extends ReplicationEvent {

    private final String ownerId;
    private final String currency;
    private final BigDecimal initialBalance;
    private final AccountType accountType;
    private final Instant createdAt;
    private final Instant updatedAt;

    public AccountCreationEvent(String commandId,
                                String source,
                                String ownerId,
                                String currency,
                                BigDecimal initialBalance,
                                AccountType accountType,
                                Instant createdAt,
                                Instant updatedAt) {
        super(EventType.ACCOUNT_CREATED, commandId, source);
        this.ownerId = ownerId;
        this.currency = currency;
        this.initialBalance = initialBalance;
        this.accountType = accountType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getOwnerId() { return ownerId; }
    public String getCurrency() { return currency; }
    public BigDecimal getInitialBalance() { return initialBalance; }
    public AccountType getAccountType() { return accountType; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
