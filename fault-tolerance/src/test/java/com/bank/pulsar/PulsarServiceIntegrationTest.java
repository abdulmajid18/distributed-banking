package com.bank.pulsar;

import com.bank.pulsar.exception.MessageServiceException;
import org.apache.pulsar.client.api.*;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PulsarServiceIntegrationTest {

    private static PulsarContainer pulsarContainer;
    private PulsarService pulsarService;

    @BeforeAll
    static void startContainer() {
        pulsarContainer = new PulsarContainer(DockerImageName.parse("apachepulsar/pulsar"))
                .withFunctionsWorker();

        pulsarContainer.start();
    }

    @AfterAll
    static void stopContainer() {
        pulsarContainer.stop();
    }

    @BeforeEach
    void setUp() {
        LoadConfig config = new LoadConfig() {
            @Override
            public String getPulsarUrl() {
                return pulsarContainer.getPulsarBrokerUrl();
            }

            @Override
            public String getLogTopic() {
                return "test-log-topic";
            }

            @Override
            public String getDebeziumTopic() {
                return "test-debezium-topic";
            }

            @Override
            public String getDebeziumSubscription() {
                return "sub-debezium";
            }

            @Override
            public String getLogSubscription() {
                return "sub-log";
            }
        };

        pulsarService = new PulsarService(config);
    }

    @Test
    void testStart_SuccessfulConnection() throws Exception {
        // Act
        assertDoesNotThrow(() -> pulsarService.start(),
                "Start should not throw exception with valid Pulsar URL");

        // Assert
        assertNotNull(pulsarService.getClient(), "Pulsar client should be initialized");
    }


    @Test
    void testNewLogProducer() throws Exception {
        // Start Pulsar service
        pulsarService.start();

        // Test producer creation
        assertDoesNotThrow(() -> pulsarService.newLogProducer(),
                "Producer creation should not throw exception");

        Producer<byte[]> producer = pulsarService.getLogProducer();
        assertNotNull(producer, "Producer should be created");
        assertTrue(producer.isConnected(), "Producer should be connected");

        // Test message sending
        String testMessage = "Test message for producer";
        MessageId messageId = producer.send(testMessage.getBytes());
        assertNotNull(messageId, "Message should be sent successfully");

        // Verify the topic name
        assertEquals("test-log-topic", producer.getTopic(),
                "Producer should be configured with correct topic");
    }

    @Test
    void testNewLogProducer_WithoutStart() {
        // Try to create producer without starting the service first
        MessageServiceException exception = assertThrows(MessageServiceException.class,
                () -> pulsarService.newLogProducer());

        // This assumes your PulsarService requires start() to be called first
        assertTrue(exception.getMessage().contains("Failed to create log producer"));
    }

    @Test
    void testNewLogConsumer() throws Exception {
        // arrange
        pulsarService.start();
        // Act
        assertDoesNotThrow(() -> pulsarService.newLogConsumer(),
                "Consumer creation should not throw exception");

        // Assert
        Consumer<byte[]> consumer = pulsarService.getLogConsumer();
        assertNotNull(consumer, "Consumer should be created");
        assertTrue(consumer.isConnected(), "Consumer should be connected");
        assertEquals("test-log-topic", consumer.getTopic(),
                "Consumer should be subscribed to correct topic");
        assertEquals("sub-log", consumer.getSubscription(),
                "Consumer should have correct subscription name");
    }

    @Test
    void testSendLogMessage_SuccessfulSend() throws Exception {
        // Arrange
        String testMessage = "Test log message for banking system";
        byte[] messageBytes = testMessage.getBytes(StandardCharsets.UTF_8);
        pulsarService.start();
        pulsarService.newLogProducer();

        // Act
        assertDoesNotThrow(() -> pulsarService.sendLogMessage(messageBytes),
                "sendLogMessage should not throw exception");

    }


    @Test
    void testConsumeLogMessages() throws Exception {
        // Arrange
        pulsarService.start();
        pulsarService.newLogProducer();
        pulsarService.newLogConsumer();
        String testMessage = "Test single message";
        pulsarService.sendLogMessage(testMessage.getBytes());

        AtomicBoolean messageReceived = new AtomicBoolean(false);
        AtomicInteger receivedCount = new AtomicInteger(0);

        // Use a separate thread for consumption since it's a blocking loop
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                pulsarService.consumeLogMessages();
                messageReceived.set(true);
                receivedCount.incrementAndGet();

            } catch (MessageServiceException e) {
                throw new RuntimeException(e);
            }
        });

        pulsarService.stopLogConsuming();

        // Act & Assert
        future.get(10, TimeUnit.SECONDS); // Wait with timeout
        assertTrue(messageReceived.get(), "Message should be received and processed");
        assertEquals(1, receivedCount.get(), "Should receive exactly one message");

        executor.shutdown();
    }

        @Test
    void testPulsarConnectionAndMessageFlow() throws Exception {
        /// Start Pulsar client
        pulsarService.start();

        // Create producer and consumer
        pulsarService.newLogProducer();
        pulsarService.newLogConsumer();

          // Send a message
        String message = "Integration Test Message";
        pulsarService.sendLogMessage(message.getBytes());

       // Receive the message via the consumer
        Message<byte[]> received = pulsarService.getLogConsumer().receive(5, java.util.concurrent.TimeUnit.SECONDS);

        assertNotNull(received, "Message should be received");
        assertEquals(message, new String(received.getData()), "Received message should match sent message");

        // Acknowledge and clean up
        pulsarService.getLogConsumer().acknowledge(received);
        pulsarService.getLogConsumer().close();
        pulsarService.getLogProducer().close();

    }

    @Test
    void testNewDebeziumConsumer() throws Exception {
        // arrange
        pulsarService.start();
        // Act
        assertDoesNotThrow(() -> pulsarService.newDebeziumConsumer(),
                "Debezium Consumer creation should not throw exception");

        // Assert
        Consumer<byte[]> consumer = pulsarService.getDebeziumConsumer();
        assertNotNull(consumer, "Consumer should be created");
        assertTrue(consumer.isConnected(), "Consumer should be connected");
        assertEquals("test-debezium-topic", consumer.getTopic(),
                "Consumer should be subscribed to correct topic");
        assertEquals("sub-debezium", consumer.getSubscription(),
                "Consumer should have correct subscription name");
    }

    @Test
    void testConsumeDebeziumMessages_WithDelayedStop() throws Exception {
        PulsarClient testClient = PulsarClient.builder()
                .serviceUrl(pulsarContainer.getPulsarBrokerUrl())
                .build();
        Producer<byte[]> testDebeziumProducer = testClient.newProducer()
                .topic("test-debezium-topic")
                .create();

        // Arrange
        pulsarService.start();
        pulsarService.newDebeziumConsumer();
        pulsarService.newLogProducer(); // Need this for forwarding

        String testMessage = "Test single message";

        AtomicBoolean messageReceived = new AtomicBoolean(false);
        CountDownLatch consumptionStartedLatch = new CountDownLatch(1);

        // Use a separate thread for consumption
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                consumptionStartedLatch.countDown();
                // This will run until runningDebezium becomes false
                pulsarService.consumeDebeziumMessages();
                messageReceived.set(true);
            } catch (MessageServiceException e) {
                throw new RuntimeException(e);
            }
        });

        // Wait for consumption to start
        assertTrue(consumptionStartedLatch.await(5, TimeUnit.SECONDS),
                "Consumption should start within timeout");

        // Give it a moment to start the loop
        Thread.sleep(100);

        // Send message
        testDebeziumProducer.send(testMessage.getBytes());

        // Give it time to process the message
        Thread.sleep(2000);

        // Now stop the consumption
        pulsarService.stopDebeziumConsuming();

        // Act & Assert
        future.get(10, TimeUnit.SECONDS); // Wait with timeout
        assertTrue(messageReceived.get(), "Message should be received and processed");

        executor.shutdown();
        testDebeziumProducer.close();
        testClient.close();
    }
}

