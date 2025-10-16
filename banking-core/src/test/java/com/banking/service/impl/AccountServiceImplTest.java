package com.banking.service.impl;

import com.banking.dao.AccountDao;
import com.banking.dao.exception.AccountCreationException;
import com.banking.dao.impl.AccountDaoImpl;
import com.banking.dto.CreateAccountDto;
import com.banking.model.Account;
import com.banking.model.AccountType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    private static Connection connection;

    private AccountServiceImpl accountService;

    private static AccountDao dao;

    @BeforeAll
    static void setupDatabase() throws SQLException, SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        Statement stmt = connection.createStatement();
        stmt.execute("CREATE TABLE accounts (" +
                "account_id VARCHAR PRIMARY KEY, " +
                "number VARCHAR, " +
                "owner_id VARCHAR, " +
                "balance DECIMAL, " +
                "currency VARCHAR, " +
                "account_type VARCHAR, " +
                "status VARCHAR, " +
                "created_at TIMESTAMP, " +
                "updated_at TIMESTAMP)");
        dao = new AccountDaoImpl(connection);
    }


    @BeforeEach
    void setup() throws SQLException {
        accountService = new AccountServiceImpl(dao, null);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM accounts");
        }
    }

    @Test
    void createAccount() throws SQLException {
        CreateAccountDto dto = new CreateAccountDto(
                "user123",
                "USD",
                new BigDecimal("5.00"),
                AccountType.SAVINGS,
                Instant.now(),
                Instant.now()
        );
        Account createdAccount = accountService.createAccount(dto);

        assertThat(createdAccount).isNotNull();
        assertThat(createdAccount.getOwnerId()).isEqualTo("user123");
        assertThat(createdAccount.getCurrency()).isEqualTo("USD");
        assertThat(createdAccount.getBalance()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(createdAccount.getAccountType()).isEqualTo(AccountType.SAVINGS);
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