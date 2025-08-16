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

//doAnswer(invocation -> {
//Account acc = invocation.getArgument(0);
//    System.out.println("Saving account: " + acc.getAccountId());
//        return null; // must return null for void methods
//        }).when(accountDao).save(any(Account.class));
