package com.banking.model;

import com.banking.service.exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a bank account in the distributed banking system.
 */
public final class Account {
    private final String accountId;
    private final String number;
    private final String ownerId;
    private BigDecimal balance;
    private final String currency;
    private final AccountType accountType;
    private AccountStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Account(Builder builder) {
        this.accountId = builder.accountId;
        this.number = builder.accountNumber;
        this.ownerId = builder.ownerId;
        this.balance = builder.balance;
        this.currency = builder.currency;
        this.accountType = builder.type;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static class Builder {
        private String accountId;
        private String accountNumber;
        private String ownerId;
        private BigDecimal balance = BigDecimal.ZERO;
        private String currency;
        private AccountType type;
        private AccountStatus status = AccountStatus.ACTIVE;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public Builder() {}

        public Builder(Account account) {
            this.accountId = account.accountId;
            this.accountNumber = account.number;
            this.ownerId = account.ownerId;
            this.balance = account.balance;
            this.currency = account.currency;
            this.type = account.accountType;
            this.status = account.status;
            this.createdAt = account.createdAt;
            this.updatedAt = account.updatedAt;
        }

        public Builder accountId(String accountId) { this.accountId = accountId; return this; }
        public Builder accountNumber(String accountNumber) { this.accountNumber = accountNumber; return this; }
        public Builder ownerId(String ownerId) { this.ownerId = ownerId; return this; }
        public Builder balance(BigDecimal balance) { this.balance = balance; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder type(AccountType type) { this.type = type; return this; }
        public Builder status(AccountStatus status) { this.status = status; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Account build() {
            return new Account(this);
        }
    }

    // Getters only (no setters)
    public String getAccountId() { return accountId; }
    public String getNumber() { return number; }
    public String getOwnerId() { return ownerId; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrency() { return currency; }
    public AccountType getAccountType() { return accountType; }
    public AccountStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public boolean isActive() { return status == AccountStatus.ACTIVE; }
    public boolean canWithdraw(BigDecimal amount) { return balance.compareTo(amount) >= 0; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return accountId.equals(account.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    //=== Core Domain Operations ===//

    /**
     * Factory method for account creation (Domain Logic)
     */

    public static Account create(String ownerId, String currency, AccountType accountType,
                                 BigDecimal initialBalance) {
        return create(ownerId, currency, accountType, initialBalance, Instant.now(), Instant.now());
    }

    public static Account create(String ownerId, String currency, AccountType accountType,
                                 BigDecimal initialBalance, Instant createdAt, Instant updatedAt) {
        return new Builder()
                .accountId(java.util.UUID.randomUUID().toString())
                .accountNumber(generateAccountNumber())
                .ownerId(ownerId)
                .currency(currency)
                .type(accountType)
                .status(AccountStatus.ACTIVE)
                .balance(initialBalance)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * Deposits money into the account (Domain Logic)
     * @throws IllegalStateException if account is not active
     */
    public void deposit(BigDecimal amount) {
        validateActiveAccount();
        this.balance = balance.add(amount);
        this.updatedAt = Instant.now();
    }

    /**
     * Withdraws money from the account (Domain Logic)
     * @throws InsufficientFundsException if balance < amount
     * @throws IllegalStateException if account is not active
     */
    public void withdraw(BigDecimal amount) {
        validateActiveAccount();
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }
        this.balance = balance.subtract(amount);
        this.updatedAt = Instant.now();
    }

    /**
     * Freezes the account (Domain Logic)
    */
    public void freeze() {
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Closed accounts cannot be frozen");
        }
        this.status = AccountStatus.FROZEN;
        this.updatedAt = Instant.now();
    }

    /**
     * Closes the account (Domain Logic)
     * @throws IllegalStateException if balance != 0
     */
    public void close() {
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Account balance must be zero before closing");
        }
        this.status = AccountStatus.CLOSED;
        this.updatedAt = Instant.now();
    }

    //=== Validation Helpers ===//
    private void validateActiveAccount() {
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return amount;
    }

    private BigDecimal validateBalance(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        return balance;
    }

    private static String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis();
    }
}
