package com.bank.bookkeeper;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Objects;

public class LoadConfig {
    private final String host;
    private final String port;
    private final String ledgerPassword;
    private final String ledgerName;

    public LoadConfig() {
        Dotenv dotenv = Dotenv.load();

        this.host = dotenv.get("ZOOKEEPER_HOST", "localhost");
        this.port = dotenv.get("ZOOKEEPER_PORT", "2181");
        this.ledgerPassword = dotenv.get("LEDGER_PASSWORD", "password");
        this.ledgerName = dotenv.get("LEDGER_NAME", "default-ledger");
    }

    public String getHost() {
        return Objects.requireNonNull(host, "Zookeeper host not found");
    }

    public String getPort() {
        return Objects.requireNonNull(port, "Zookeeper port not found");
    }

    public String getLedgerPassword() {
        return Objects.requireNonNull(ledgerPassword, "Ledger password not found");
    }

    public String getLedgerName() {
        return Objects.requireNonNull(ledgerName, "Ledger name not found");
    }
}
