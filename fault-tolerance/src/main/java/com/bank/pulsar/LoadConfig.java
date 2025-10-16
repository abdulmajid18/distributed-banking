package com.bank.pulsar;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Objects;

/**
 * LoadConfig:
 * Loads Pulsar connection and topic configuration from .env or environment variables.
 */
public class LoadConfig {

    private final String pulsarUrl;
    private final String adminUrl;

    private final String debeziumTopic;
    private final String logTopic;

    private final String debeziumSubscription;
    private final String logSubscription;

    public LoadConfig() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        // Pulsar connection details
        this.pulsarUrl = dotenv.get("PULSAR_SERVICE_URL", "pulsar://localhost:6650");

        // Pulsar Admin service (HTTP) endpoint for PulsarAdmin API
        this.adminUrl = dotenv.get("PULSAR_ADMIN_URL", "http://localhost:8080");

        // Topics
        this.debeziumTopic = dotenv.get("PULSAR_DEBEZIUM_TOPIC", "debezium-topic");
        this.logTopic = dotenv.get("PULSAR_LOG_TOPIC", "log-topic");

        // Subscriptions
        this.debeziumSubscription = dotenv.get("PULSAR_DEBEZIUM_SUBSCRIPTION", "debezium-subscription");
        this.logSubscription = dotenv.get("PULSAR_LOG_SUBSCRIPTION", "log-subscription");
    }

    public String getPulsarUrl() {
        return Objects.requireNonNull(pulsarUrl, "Pulsar URL not found");
    }

    public String getAdminUrl() {
        return Objects.requireNonNull(adminUrl, "Pulsar admin URL not found");
    }

    public String getDebeziumTopic() {
        return Objects.requireNonNull(debeziumTopic, "Debezium topic name not found");
    }

    public String getLogTopic() {
        return Objects.requireNonNull(logTopic, "Log topic name not found");
    }

    public String getDebeziumSubscription() {
        return Objects.requireNonNull(debeziumSubscription, "Debezium subscription name not found");
    }

    public String getLogSubscription() {
        return Objects.requireNonNull(logSubscription, "Log subscription name not found");
    }

    @Override
    public String toString() {
        return String.format(
                "Pulsar Config [pulsarUrl=%s, adminUrl=%s, debeziumTopic=%s, logTopic=%s, debeziumSubscription=%s, logSubscription=%s]",
                pulsarUrl, adminUrl, debeziumTopic, logTopic, debeziumSubscription, logSubscription
        );
    }
}
