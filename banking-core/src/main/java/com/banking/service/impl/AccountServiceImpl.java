package com.banking.service.impl;

import com.banking.dao.AccountDao;
import com.banking.dao.exception.AccountCreationException;
import com.banking.dto.CreateAccountDto;
import com.banking.events.dao.AccountCreationEventDao;
import com.banking.events.model.AccountCreationEvent;
import com.banking.model.Account;
import com.banking.service.AccountService;

import java.math.BigDecimal;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountServiceImpl implements AccountService {
    private final AccountDao accountDao;

    private final AccountCreationEventDao accountCreationEventDao;

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    public AccountServiceImpl(AccountDao accountDao, AccountCreationEventDao accountCreationEventDao) {
        this.accountDao = accountDao;
        this.accountCreationEventDao = accountCreationEventDao;
    }

    @Override
    public Account createAccount(CreateAccountDto createAccountDto) {
        logger.info("Attempting to create account for owner: {}", createAccountDto.ownerId());
        try {
            Account newAccount = Account.create(
                    createAccountDto.ownerId(),
                    createAccountDto.currency(),
                    createAccountDto.accountType(),
                    createAccountDto.initialBalance(),
                    createAccountDto.createdAt(),
                    createAccountDto.updatedAt()
            );
            logger.debug("Created account object: {}", newAccount);
            accountDao.save(newAccount);
            logger.info("Successfully created account with ID: {}", newAccount.getAccountId());

            AccountCreationEvent event = new AccountCreationEvent(
                    /* commandId = */ "CMD-" + newAccount.getAccountId(),
                    /* source = */ "AccountService",
                    /* business data */
                    newAccount.getOwnerId(),
                    newAccount.getCurrency(),
                    newAccount.getBalance(),
                    newAccount.getAccountType(),
                    newAccount.getCreatedAt(),
                    newAccount.getUpdatedAt()
            );

            accountCreationEventDao.save(event);
            logger.info("Saved AccountCreationEvent for account ID: {}", newAccount.getAccountId());
            return newAccount;

        } catch (SQLException e) {
            logger.error("Failed to create account for owner: {}. Error: {}",
                    createAccountDto.ownerId(), e.getMessage(), e);
            throw new AccountCreationException("Failed to create account", e);
        }
    }

    @Override
    public void deposit(String accountId, BigDecimal amount) {


    }

    @Override
    public void withdraw(String accountId, BigDecimal amount) {

    }

    @Override
    public void transfer(String sourceId, String targetId, BigDecimal amount) {

    }

    @Override
    public BigDecimal getBalance(String accountId) {
        return null;
    }

    @Override
    public void freezeAccount(String accountId) {

    }

    @Override
    public void closeAccount(String accountId) {

    }
}
