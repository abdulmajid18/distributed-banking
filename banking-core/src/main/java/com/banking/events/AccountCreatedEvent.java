package com.banking.events;


import com.banking.dto.CreateAccountDto;

public class AccountCreatedEvent extends ReplicationEvent {
    private final String ownerId;
    private final String currency;
    private final String initialBalance;
    private final String accountType;

    public AccountCreatedEvent(
            String commandId,
            String source,
            byte[] commandData,
            CreateAccountDto dto
    ) {
        super(EventType.ACCOUNT_CREATED, commandId, source, commandData);
        this.ownerId = dto.ownerId();
        this.currency = dto.currency();
        this.initialBalance = dto.initialBalance().toString();
        this.accountType = dto.accountType().name();
    }

    // Getters
    public String getOwnerId() { return ownerId; }
    public String getCurrency() { return currency; }
    public String getInitialBalance() { return initialBalance; }
    public String getAccountType() { return accountType; }
}
