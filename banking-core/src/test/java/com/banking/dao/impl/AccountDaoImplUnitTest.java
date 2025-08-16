package com.banking.dao.impl;

import com.banking.dao.AccountDao;
import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.AccountType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountDaoImplUnitTest {

    @Mock
    private Connection mockConnection;

    @Test
    void save(@Mock PreparedStatement mockStmt) throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
        Account account = new Account.Builder()
                .accountId("A1")
                .accountNumber("12345")
                .ownerId("U1")
                .balance(BigDecimal.valueOf(100))
                .currency("USD")
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .build();
        AccountDao dao = new AccountDaoImpl(mockConnection);
        dao.save(account);
        verify(mockConnection).prepareStatement(anyString());
        verify(mockStmt).executeUpdate();
        verify(mockStmt).close();
        verify(mockStmt).setString(1, "A1");
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