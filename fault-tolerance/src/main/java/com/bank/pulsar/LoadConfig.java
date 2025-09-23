package com.bank.pulsar;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

public class LoadConfig {
    private final String host;
    private final String port;
    private final String topic;

    public LoadConfig() {
        Dotenv dotenv = Dotenv.load();

        this.host = dotenv.get("PULSAR_HOST", "localhost");
        this.port = dotenv.get("PULSAR_PORT", "6650");
        this.topic = dotenv.get("PULSAR_TOPIC", "pulsar-topic");
    }

    public String getHost() {
        return Objects.requireNonNull(host, "pulsar host not found");
    }

    public String getPort() {
        return Objects.requireNonNull(port, "pulsar port not found");
    }

    public String getTopic() {
        return Objects.requireNonNull(topic, "Topic name not found");
    }
}
