package com.bank.pulsar;


import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class PulsarService {

    private static final Logger logger = LoggerFactory.getLogger(PulsarService.class);

    private final LoadConfig config;

    private PulsarClient client;

    private Producer<byte[]> producer;

    private Consumer<byte[]> consumer;

    public PulsarService(LoadConfig config) {
        this.config = config;
    }

    public void connectClient() throws PulsarClientException {
        String url = "pulsar://" + config.getHost() + ":" + config.getPort();
        client = PulsarClient.builder().serviceUrl(url).build();
        logger.info("Connected to Pulsar Client at {}", url);
    }

    public void newProducer() throws PulsarClientException {
        producer = client.newProducer()
                .topic(config.getTopic())
                .compressionType(CompressionType.LZ4)
                .create();
        logger.info("Topic created for Pulsar {}", config.getTopic());
    }

    public void sendMessageToTopic(byte[] message) throws PulsarClientException {
        TypedMessageBuilder<byte[]> messageBuilder = producer.newMessage();
        messageBuilder.value(message)
                .key("my-key")
                .property("banking", "primary")
                .eventTime(System.currentTimeMillis());
        MessageId messageId = messageBuilder.send();
        logger.info("Message sent with ID: {}", messageId);
    }

    public void newConsumer() throws PulsarClientException {
        consumer = client.newConsumer()
                .topic(config.getTopic())
                .subscriptionName("test-subscription")
                .subscriptionType(SubscriptionType.Shared)
                .subscribe();
        logger.info("Consumer subscribed t o Pulsar topic {}", config.getTopic());
    }

    public void receiveMessages() throws PulsarClientException {
        while (true) {
            // Wait up to 1 second for a message
            Message<byte[]> msg = consumer.receive(1, TimeUnit.SECONDS);
            if (msg != null) {
                try {
                    String received = new String(msg.getData());
                    logger.info("Received message with ID {} and content: {}", msg.getMessageId(), received);

                    // Acknowledge so it won’t be redelivered
                    consumer.acknowledge(msg);
                } catch (Exception e) {
                    consumer.negativeAcknowledge(msg);
                }
            }
        }
    }

    public void close() throws PulsarClientException {
        if (producer != null) producer.close();
        if (consumer != null) consumer.close();
        if (client != null) client.close();
        logger.info("Closed Pulsar client, producer, and consumer");
    }
}
