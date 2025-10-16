package com.bank.pulsar;

import com.bank.pulsar.exception.MessageServiceException;
import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * PulsarService:
 *  - Consumes Debezium CDC messages from a Pulsar topic
 *  - Forwards them to a log topic using a log producer
 *  - Allows replicas to consume messages from the log topic
 */
public class PulsarService implements MessageService {
    private static final Logger logger = LoggerFactory.getLogger(PulsarService.class);

    private final LoadConfig config;
    private PulsarClient client;

    // Separate producer and consumers
    private Producer<byte[]> logProducer;
    private Consumer<byte[]> debeziumConsumer;
    private Consumer<byte[]> logConsumer;

    // Control flags for stopping consumption loops
    private volatile boolean runningDebezium = true;
    private volatile boolean runningLog = true;

    public PulsarService(LoadConfig config) {
        this.config = config;
    }

    protected PulsarClient createPulsarClient(String url) throws PulsarClientException {
        return PulsarClient.builder().serviceUrl(url).build();
    }

    public PulsarClient getClient() {
        return client;
    }
    /**
     * Initializes Pulsar client connection.
     */
    @Override
    public void start() throws MessageServiceException {
        try {
            String url = config.getPulsarUrl();
            client = createPulsarClient(url);
            logger.info("Connected to Pulsar Client at {}", url);
        } catch (PulsarClientException | NullPointerException e) {
            throw new MessageServiceException("Failed to start Pulsar service", e);
        }
    }


    public  Producer<byte[]>  getLogProducer() {
        return logProducer;
    }

    /**
     * Creates a new log producer that forwards messages to the log topic.
     */
    public void newLogProducer() throws MessageServiceException {
        try {
            logProducer = client.newProducer()
                    .topic(config.getLogTopic()) // log topic
                    .compressionType(CompressionType.LZ4)
                    .create();
            logger.info("Log producer created for Pulsar topic: {}", config.getLogTopic());
        } catch (PulsarClientException | NullPointerException e) {
            throw new MessageServiceException("Failed to create log producer", e);
        }
    }

    public  Consumer<byte[]>  getLogConsumer() {
        return logConsumer;
    }

    /**
     * Creates a log consumer (for replicas).
     */
    public void newLogConsumer() throws MessageServiceException {
        try {
            logConsumer = client.newConsumer()
                    .topic(config.getLogTopic()) // log topic
                    .subscriptionName(config.getLogSubscription())
                    .subscriptionType(SubscriptionType.Shared)
                    .subscribe();
            logger.info("Log consumer subscribed to topic: {}", config.getLogTopic());
        } catch (PulsarClientException e) {
            throw new MessageServiceException("Failed to create log consumer", e);
        }
    }

    public  Consumer<byte[]>  getDebeziumConsumer() {
        return debeziumConsumer;
    }

    /**
     * Creates a Debezium consumer that listens to the CDC topic.
     */
    public void newDebeziumConsumer() throws MessageServiceException {
        try {
            debeziumConsumer = client.newConsumer()
                    .topic(config.getDebeziumTopic())
                    .subscriptionName(config.getDebeziumSubscription())
                    .subscriptionType(SubscriptionType.Shared)
                    .subscribe();
            logger.info("Debezium consumer subscribed to topic: {}", config.getDebeziumTopic());
        } catch (PulsarClientException e) {
            throw new MessageServiceException("Failed to create Debezium consumer", e);
        }
    }


    /**
     * Consumes messages from Debezium topic and forwards them to the log topic.
     */
    public void consumeDebeziumMessages() throws MessageServiceException {
        logger.info("Starting Debezium consumption loop...");
        while (runningDebezium) {
            try {
                Message<byte[]> msg = debeziumConsumer.receive(1, TimeUnit.SECONDS);
                if (msg != null) {
                    String received = new String(msg.getData(), StandardCharsets.UTF_8);
                    logger.info("Debezium message received: ID={} content={}", msg.getMessageId(), received);

                    // Forward to log topic
                    sendLogMessage(msg.getData());

                    debeziumConsumer.acknowledge(msg);
                }
            } catch (PulsarClientException e) {
                throw new MessageServiceException("Failed to receive Debezium messages", e);
            }
        }
        logger.info("Stopped Debezium consumption loop.");
    }

    /**
     * Consumes log messages (used by replicas).
     */
    public void consumeLogMessages() throws MessageServiceException {
        logger.info("Starting log consumption loop...");
        while (runningLog) {
            try {
                Message<byte[]> msg = logConsumer.receive(1, TimeUnit.SECONDS);
                if (msg != null) {
                    String received = new String(msg.getData(), StandardCharsets.UTF_8);
                    logger.info("Replica received log message: ID={} content={}", msg.getMessageId(), received);
                    logConsumer.acknowledge(msg);
                }
            } catch (PulsarClientException e) {
                throw new MessageServiceException("Failed to receive log messages", e);
            }
        }
        logger.info("Stopped log consumption loop.");
    }

    /**
     * Sends a log message to the configured log topic.
     */
    @Override
    public void sendLogMessage(byte[] message) throws MessageServiceException {
        try {
            TypedMessageBuilder<byte[]> messageBuilder = logProducer.newMessage();
            messageBuilder.value(message)
                    .key("my-key")
                    .property("banking", "primary")
                    .eventTime(System.currentTimeMillis());
            MessageId messageId = messageBuilder.send();
            logger.info("Message forwarded to log topic with ID: {}", messageId);
        } catch (PulsarClientException | NullPointerException e) {
            throw new MessageServiceException("Failed to send log message", e);
        }
    }

    /**
     * Stops the Debezium consumer loop.
     */
    public void stopDebeziumConsuming() {
        runningDebezium = false;
        logger.info("Stopping Debezium consumer...");
    }

    /**
     * Stops the log consumer loop.
     */
    public void stopLogConsuming() {
        runningLog = false;
        logger.info("Stopping log consumer...");
    }

    /**
     * Closes all Pulsar resources.
     */
    @Override
    public void close() throws MessageServiceException {
        try {
            if (logProducer != null) logProducer.close();
            if (debeziumConsumer != null) debeziumConsumer.close();
            if (logConsumer != null) logConsumer.close();
            if (client != null) client.close();
            logger.info("Closed Pulsar client, producer, and consumers.");
        } catch (PulsarClientException e) {
            throw new MessageServiceException("Failed to close Pulsar resources", e);
        }
    }


}
