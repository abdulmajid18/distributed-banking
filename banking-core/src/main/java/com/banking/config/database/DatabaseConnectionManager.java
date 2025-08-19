package com.banking.config.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionManager {
    private static DatabaseConnectionManager instance;
    private final HikariDataSource dataSource;

    private DatabaseConnectionManager() {
        DatabaseLoadEnv config = new DatabaseLoadEnv();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getDatabaseUrl());
        hikariConfig.setUsername(config.getDatabaseUser());
        hikariConfig.setPassword(config.getDatabasePassword());

        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setIdleTimeout(30000); // 30 seconds
        hikariConfig.setConnectionTimeout(30000); // 30 seconds
        hikariConfig.setPoolName("BankingPool");

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public static DatabaseConnectionManager getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (DatabaseConnectionManager.class) {
            if (instance == null) {
                instance = new DatabaseConnectionManager();
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if (instance != null && instance.dataSource != null) {
            instance.dataSource.close();
            instance = null;
        }
    }
}
