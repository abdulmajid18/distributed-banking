package com.banking.grpc;

import com.banking.dao.AccountDao;
import com.banking.dto.CreateAccountDto;
import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.AccountType;
import com.banking.service.AccountService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountGrpcServiceUnitTest {

    @Mock
    private AccountService accountService;

    @Mock
    private StreamObserver<AccountResponse> responseObserver;

    @Captor
    private ArgumentCaptor<AccountResponse> responseCaptor;

    @Captor
    private ArgumentCaptor<Throwable> errorCaptor;

    @InjectMocks
    private AccountGrpcService accountGrpcService;

    private CreateAccountRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = CreateAccountRequest.newBuilder()
                .setOwnerId("user-123")
                .setCurrency("USD")
                .setInitialBalance("1000.00")
                .setAccountType("CHECKING")
                .build();
    }

    private Account createTestAccount() {
        return new Account.Builder()
                .accountId(String.valueOf(UUID.randomUUID()))
                .accountNumber("ACC123456789")
                .ownerId("user-123")
                .balance(new BigDecimal("1500.00"))
                .currency("USD")
                .type(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void createAccount_Success() {
        Account expectedAccount = createTestAccount();
        when(accountService.createAccount(any(CreateAccountDto.class))).thenReturn(expectedAccount);

        accountGrpcService.createAccount(validRequest, responseObserver);
        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());

        AccountResponse response = responseCaptor.getValue();

        assertThat(response)
                .isNotNull()
                .extracting(
                        AccountResponse::getAccountId,
                        AccountResponse::getAccountNumber,
                        AccountResponse::getOwnerId,
                        AccountResponse::getBalance,
                        AccountResponse::getCurrency,
                        AccountResponse::getAccountType,
                        AccountResponse::getStatus
                )
                .containsExactly(
                        expectedAccount.getAccountId(),
                        expectedAccount.getNumber(),
                        expectedAccount.getOwnerId(),
                        expectedAccount.getBalance().toString(),
                        expectedAccount.getCurrency(),
                        expectedAccount.getAccountType().name(),
                        expectedAccount.getStatus().name()
                );
    }

    @Test
    void createAccount_ServiceThrowsException_ShouldPropagateError() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Service error");
        when(accountService.createAccount(any(CreateAccountDto.class)))
                .thenThrow(expectedException);

        // Act
        accountGrpcService.createAccount(validRequest, responseObserver);

        // Assert
        verify(responseObserver).onError(errorCaptor.capture());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();

        Throwable actualError = errorCaptor.getValue();
        assertEquals(expectedException, actualError);

        verify(responseObserver).onError(expectedException);
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }

}