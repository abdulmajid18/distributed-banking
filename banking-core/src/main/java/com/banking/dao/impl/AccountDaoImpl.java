package com.banking.dao.impl;

import com.banking.dao.AccountDao;
import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.AccountType;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class AccountDaoImpl implements AccountDao {
    private final Connection connection;

    public AccountDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (account_id, number, owner_id, balance, " +
                "currency, account_type, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setAccountParameters(stmt, account);
            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<Account> findById(String accountId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? Optional.of(fromResultSet(rs)) : Optional.empty();
        }
    }

    @Override
    public Optional<Account> findByNumber(String number) {
        return Optional.empty();
    }

    @Override
    public void update(Account account) {

    }

    @Override
    public void delete(String id) {

    }

    @Override
    public List<Account> findAll() {
        return List.of();
    }

    private void setAccountParameters(PreparedStatement stmt, Account account)
            throws SQLException {
        stmt.setString(1, account.getAccountId());
        stmt.setString(2, account.getNumber());
        stmt.setString(3, account.getOwnerId());
        stmt.setBigDecimal(4, account.getBalance());
        stmt.setString(5, account.getCurrency());
        stmt.setString(6, account.getAccountType().name());
        stmt.setString(7, account.getStatus().name());
        stmt.setTimestamp(8, Timestamp.from(account.getCreatedAt()));
        stmt.setTimestamp(9, Timestamp.from(account.getUpdatedAt()));
    }

    public static Account fromResultSet(ResultSet rs) throws SQLException {
        return new Account.Builder()
                .accountId(rs.getString("account_id"))
                .accountNumber(rs.getString("number"))
                .ownerId(rs.getString("owner_id"))
                .balance(rs.getBigDecimal("balance"))
                .currency(rs.getString("currency"))
                .type(AccountType.safeValueOf(rs.getString("account_type")))
                .status(AccountStatus.safeValueOf(rs.getString("status")))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .updatedAt(rs.getTimestamp("updated_at").toInstant())
                .build();
    }

}
