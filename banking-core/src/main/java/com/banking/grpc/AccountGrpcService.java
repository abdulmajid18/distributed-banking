package com.banking.grpc;

import com.bank.replication.ReplicationStrategy;
import com.banking.dto.CreateAccountDto;
import com.banking.model.Account;
import com.banking.service.AccountService;
import io.grpc.stub.StreamObserver;
import com.banking.model.AccountType;

import java.math.BigDecimal;

public class AccountGrpcService extends AccountServiceGrpc.AccountServiceImplBase{
    private final AccountService accountService;
    private final ReplicationStrategy replicationStrategy;

    public AccountGrpcService(AccountService accountService, ReplicationStrategy replicationStrategy) {
        this.accountService = accountService;
        this.replicationStrategy = replicationStrategy;
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<AccountResponse> responseObserver) {
        try {
            CreateAccountDto dto = new CreateAccountDto(
                    request.getOwnerId(),
                    request.getCurrency(),
                    new BigDecimal(request.getInitialBalance()),
                    AccountType.valueOf(request.getAccountType())
            );
            Account account = accountService.createAccount(dto);
            replicationStrategy.replicate();
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
