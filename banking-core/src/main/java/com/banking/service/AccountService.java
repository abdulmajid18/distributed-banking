package com.banking.service;

import com.banking.dto.CreateAccountDto;
import com.banking.model.Account;

import java.math.BigDecimal;

public interface AccountService {
    Account createAccount(CreateAccountDto createAccountDto);
    void deposit(String accountId, BigDecimal amount);
    void withdraw(String accountId, BigDecimal amount);
    void transfer(String sourceId, String targetId, BigDecimal amount);
    BigDecimal getBalance(String accountId);
    void freezeAccount(String accountId);
    void closeAccount(String accountId);
}

