package com.banking.config.database;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

public class DatabaseLoadEnv {

    private final String url;
    private final String username;
    private final String password;

    public DatabaseLoadEnv() {
        Dotenv dotenv = Dotenv.load();

        String host = dotenv.get("SQL_HOST", "localhost");
        String port = dotenv.get("SQL_PORT", "5432");
        String database = dotenv.get("SQL_DATABASE", "default_db");
        this.username = dotenv.get("SQL_USER", "postgres");
        this.password = dotenv.get("SQL_PASSWORD", "postgres");

        this.url = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
    }

    public String getDatabaseUrl() {
        return Objects.requireNonNull(url, "Database URL not found");
    }

    public String getDatabaseUser() {
        return Objects.requireNonNull(username, "Database username not found");
    }

    public String getDatabasePassword() {
        return Objects.requireNonNull(password, "Database password not found");
    }

    public static void main(String[] args) {
        DatabaseLoadEnv config = new DatabaseLoadEnv();
        System.out.println("URL: " + config.getDatabaseUrl());
        System.out.println("User: " + config.getDatabaseUser());
        System.out.println("Password: " + config.getDatabasePassword());
    }
}

