package com.banking.events.dao;

import com.banking.events.model.AccountCreationEvent;

import java.sql.SQLException;
import java.util.List;

public interface AccountCreationEventDao {
    void save(AccountCreationEvent event) throws SQLException;
    List<AccountCreationEvent> findAll() throws SQLException;
    void delete(String eventId) throws SQLException;
}

