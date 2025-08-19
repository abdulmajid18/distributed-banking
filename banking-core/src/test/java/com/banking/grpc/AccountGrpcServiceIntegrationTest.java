package com.banking.grpc;

import com.banking.dao.AccountDao;
import com.banking.dao.impl.AccountDaoImpl;
import com.banking.model.Account;
import com.banking.model.AccountStatus;
import com.banking.model.AccountType;
import com.banking.service.impl.AccountServiceImpl;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AccountGrpcServiceIntegrationTest {

    private static Connection connection;

    private AccountServiceImpl accountService;

    private AccountDaoImpl accountDao;

    private int port;

    private Server grpcServer;

    private ManagedChannel channel;

    private AccountServiceGrpc.AccountServiceBlockingStub blockingStub;

    @BeforeEach
    void setup() throws SQLException, IOException {
        setupDatabase();

        accountDao = new AccountDaoImpl(connection);
        accountService = new AccountServiceImpl(accountDao);

        port = findFreePort();

        setupGrpcServer(port);

        setupGrpcClient(port);
    }

    @Test
    void createAccount_Integration_Success() throws Exception {
        // Arrange
        CreateAccountRequest request = CreateAccountRequest.newBuilder()
                .setOwnerId("integration-user-1")
                .setCurrency("USD")
                .setInitialBalance("2500.00")
                .setAccountType("CHECKING")
                .build();

        // Act
        AccountResponse response = blockingStub.createAccount(request);

        // Assert - Verify gRPC response
        assertThat(response).isNotNull();
        assertThat(response.getOwnerId()).isEqualTo("integration-user-1");
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getBalance()).isEqualTo("2500.00");
        assertThat(response.getAccountType()).isEqualTo("CHECKING");
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getAccountId()).isNotBlank();
        assertThat(response.getAccountNumber()).isNotBlank();

        // Assert - Verify data was actually persisted in database
        Optional<Account> persistedAccount = accountDao.findById(response.getAccountId());
        assertThat(persistedAccount).isPresent();

        Account account = persistedAccount.get();
        assertThat(account.getOwnerId()).isEqualTo("integration-user-1");
        assertThat(account.getBalance()).isEqualByComparingTo("2500.00");
        assertThat(account.getCurrency()).isEqualTo("USD");
        assertThat(account.getAccountType()).isEqualTo(AccountType.CHECKING);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void createAccount_InvalidAccountType_ShouldThrowException() {
        // Arrange
        CreateAccountRequest request = CreateAccountRequest.newBuilder()
                .setOwnerId("user-1")
                .setCurrency("USD")
                .setInitialBalance("1000.00")
                .setAccountType("INVALID_TYPE")
                .build();

        // Act & Assert
        assertThrows(io.grpc.StatusRuntimeException.class, () -> {
            blockingStub.createAccount(request);
        });
    }


    static void setupDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        Statement stmt = connection.createStatement();
        stmt.execute("CREATE TABLE accounts (" +
                "account_id VARCHAR PRIMARY KEY, " +
                "number VARCHAR, " +
                "owner_id VARCHAR, " +
                "balance DECIMAL, " +
                "currency VARCHAR, " +
                "account_type VARCHAR, " +
                "status VARCHAR, " +
                "created_at TIMESTAMP, " +
                "updated_at TIMESTAMP)");
    }

    private int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private void setupGrpcServer(int port) throws IOException {
        grpcServer = ServerBuilder.forPort(port)
                .addService(new AccountGrpcService(accountService)).build();
        grpcServer.start();
    }

    private void setupGrpcClient(int port) {
        channel = io.grpc.ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
        blockingStub = AccountServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() throws SQLException, InterruptedException {
        // Shutdown gRPC client
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }

        // Shutdown gRPC server
        if (grpcServer != null) {
            grpcServer.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }

        // Cleanup database
        if (connection != null) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE accounts");
            }
            connection.close();
        }
    }

}