package com.bank.pulsar;

public class PulsarTest {
    public static void main(String[] args) throws Exception {
        LoadConfig config = new LoadConfig();

        PulsarService service = new PulsarService(config);
        service.connectClient();
        service.newProducer();
        service.newConsumer();

        // Send a test message
        service.sendMessageToTopic("Hello Pulsar".getBytes());

        // Start consuming
        service.receiveMessages();
    }
}
