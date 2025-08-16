package com.banking.dao.impl;

import com.banking.dao.AccountDao;
import com.banking.model.Account;
import com.banking.model.AccountStatus;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountDaoImplTest {

    private static Connection connection;
    private AccountDao dao;

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
    }

    @BeforeEach
    void setup() throws SQLException {
        dao = new AccountDaoImpl(connection);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM accounts");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM accounts");
        }
    }

    @Test
    void save() throws SQLException {
        AccountDao dao = new AccountDaoImpl(connection);
        Account account = new Account.Builder()
                .accountId("A1")
                .accountNumber("123")
                .ownerId("U1")
                .balance(new BigDecimal("100"))
                .currency("USD")
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .build();

        dao.save(account);
        Optional<Account> fetched = dao.findById("A1");
        assertTrue(fetched.isPresent());
        assertEquals("123", fetched.get().getNumber());
    }

    @Test
    void findById() {
    }

    @Test
    void findByNumber() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void findAll() {
    }

    @Test
    void fromResultSet() {
    }
}