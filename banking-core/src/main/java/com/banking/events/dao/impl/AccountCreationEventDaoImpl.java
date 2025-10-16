package com.banking.events.dao.impl;

import com.banking.events.dao.AccountCreationEventDao;
import com.banking.events.model.AccountCreationEvent;
import com.banking.model.AccountType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountCreationEventDaoImpl implements AccountCreationEventDao {
    private final Connection connection;

    public AccountCreationEventDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(AccountCreationEvent event) throws SQLException {
        String sql = """
            INSERT INTO account_creation_events (
                event_id,
                event_type,
                command_id,
                source,
                owner_id,
                currency,
                initial_balance,
                account_type,
                created_at,
                updated_at,
                timestamp
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, event.getEventId());
            stmt.setString(2, event.getEventType().name());
            stmt.setString(3, event.getCommandId());
            stmt.setString(4, event.getSource());
            stmt.setString(5, event.getOwnerId());
            stmt.setString(6, event.getCurrency());
            stmt.setBigDecimal(7, event.getInitialBalance());
            stmt.setString(8, event.getAccountType().name());
            stmt.setTimestamp(9, Timestamp.from(event.getCreatedAt()));
            stmt.setTimestamp(10, Timestamp.from(event.getUpdatedAt()));
            stmt.setLong(11, event.getTimestamp());
            stmt.executeUpdate();
        }
    }

    @Override
    public List<AccountCreationEvent> findAll() throws SQLException {
        String sql = "SELECT * FROM account_creation_events ORDER BY created_at DESC";
        List<AccountCreationEvent> events = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                events.add(fromResultSet(rs));
            }
        }

        return events;
    }

    @Override
    public void delete(String eventId) throws SQLException {
        String sql = "DELETE FROM account_creation_events WHERE event_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, eventId);
            stmt.executeUpdate();
        }
    }

    private AccountCreationEvent fromResultSet(ResultSet rs) throws SQLException {
        return new AccountCreationEvent(
                rs.getString("command_id"),
                rs.getString("source"),
                rs.getString("owner_id"),
                rs.getString("currency"),
                rs.getBigDecimal("initial_balance"),
                AccountType.valueOf(rs.getString("account_type")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
