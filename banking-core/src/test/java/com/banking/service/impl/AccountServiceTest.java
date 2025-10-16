package com.banking.service.impl;

import com.banking.dao.AccountDao;
import com.banking.dao.exception.AccountCreationException;
import com.banking.dto.CreateAccountDto;
import com.banking.model.Account;
import com.banking.model.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountDao accountDao;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    private CreateAccountDto validAccountDto;
    private static final String OWNER_ID = "user123";
    private static final String CURRENCY = "USD";
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("100.00");

    @BeforeEach
    void setUp() {
        Instant fixedTimestamp = Instant.parse("2023-10-01T10:00:00Z");

        validAccountDto = new CreateAccountDto(
                OWNER_ID,
                CURRENCY,
                INITIAL_BALANCE,
                AccountType.SAVINGS,
                fixedTimestamp,  // Add createdAt
                fixedTimestamp   // Add updatedAt
        );
    }


    @Test
    void createAccount_ShouldSuccessfullyCreateAccount() throws SQLException {
        doNothing().when(accountDao).save(any(Account.class));
        Account  createdAccount = accountService.createAccount(validAccountDto);

        assertNotNull(createdAccount);
        assertEquals(OWNER_ID, createdAccount.getOwnerId());
        assertEquals(CURRENCY, createdAccount.getCurrency());
        assertEquals(AccountType.SAVINGS, createdAccount.getAccountType() );
        assertEquals(0, INITIAL_BALANCE.compareTo(createdAccount.getBalance()));

        verify(accountDao, times(1)).save(any(Account.class));

    }

    @Test
    void createAccount_ShouldThrowExceptionWhenDaoFails() throws SQLException {
        SQLException expectedException = new SQLException("Database error");
        doThrow( expectedException ).when(accountDao).save(any(Account.class));

        assertThatThrownBy( () -> accountService.createAccount(validAccountDto) )
                .isInstanceOf(AccountCreationException.class)
                        .hasMessageContaining("Failed to create account");

        verify(accountDao).save(any(Account.class));
    }

    @Test
    void deposit() {
    }

    @Test
    void withdraw() {
    }

    @Test
    void transfer() {
    }

    @Test
    void getBalance() {
    }

    @Test
    void freezeAccount() {
    }

    @Test
    void closeAccount() {
    }
}