package com.banking.grpc;

import com.banking.dto.CreateAccountDto;
import com.banking.model.Account;
import com.banking.service.AccountService;
import io.grpc.stub.StreamObserver;
import com.banking.model.AccountType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountGrpcService extends AccountServiceGrpc.AccountServiceImplBase{
    private static final Logger logger = LoggerFactory.getLogger(AccountGrpcService.class);

    private final AccountService accountService;

    public AccountGrpcService(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<AccountResponse> responseObserver) {
        try {
            // Generate timestamps ONCE at the entry point
            Instant createdAt = Instant.now();
            CreateAccountDto dto = new CreateAccountDto(
                    request.getOwnerId(),
                    request.getCurrency(),
                    new BigDecimal(request.getInitialBalance()),
                    AccountType.valueOf(request.getAccountType()),
                    createdAt,
                    createdAt
            );
            Account account = accountService.createAccount(dto);
            responseObserver.onNext(mapToAccountResponse(account));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private AccountResponse mapToAccountResponse(Account account) {
        return AccountResponse.newBuilder()
                .setAccountId(account.getAccountId())
                .setAccountNumber(account.getNumber())
                .setOwnerId(account.getOwnerId())
                .setBalance(account.getBalance().toString())
                .setCurrency(account.getCurrency())
                .setAccountType(account.getAccountType().name())
                .setStatus(account.getStatus().name())
                .build();
    }
}
