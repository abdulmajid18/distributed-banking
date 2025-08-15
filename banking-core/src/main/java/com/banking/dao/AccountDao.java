package com.banking.dao;

import com.banking.model.Account;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AccountDao {
    void save(Account account) throws SQLException;
    Optional<Account> findById(String id) throws SQLException ;
    Optional<Account> findByNumber(String number);
    void update(Account account);
    void delete(String id);
    List<Account> findAll();
}
