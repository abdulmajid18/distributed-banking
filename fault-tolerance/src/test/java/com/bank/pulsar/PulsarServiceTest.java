package com.bank.pulsar;

import org.apache.pulsar.client.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PulsarServiceTest {

    @Mock
    private PulsarClient mockClient;

    @Mock
    private Producer<byte[]> mockProducer;

    @Mock
    private Consumer<byte[]> mockDebeziumConsumer;

    @Mock
    private TypedMessageBuilder<byte[]> mockMessageBuilder;

    @Mock
    private Message<byte[]> mockMessage;

    @InjectMocks
    private PulsarService pulsarService;

    @Mock
    private LoadConfig config;

    @BeforeEach

    void setUp() throws Exception {
        when(config.getPulsarUrl()).thenReturn("pulsar://localhost:6650");
//        lenient().when(config.getDebeziumTopic()).thenReturn("debezium-topic");
//        lenient().when(config.getDebeziumSubscription()).thenReturn("debezium-subscription");
//        lenient().when(config.getLogSubscription()).thenReturn("log-subscription");

        // Create a real instance and spy on it
        pulsarService = spy(new PulsarService(config));
    }


    @Test
    void testStartShouldCreateClient() throws Exception {
        doReturn(mockClient).when(pulsarService).createPulsarClient(anyString());
        // Simulate normal start
        pulsarService.start();
        // Normally you'd verify logger info; here we just ensure no exception thrown
        verify(pulsarService).createPulsarClient("pulsar://localhost:6650");
    }


    @Test
    void testNewLogProducerCreatesProducer() throws Exception {
        when(config.getLogTopic()).thenReturn("log-topic");
        ProducerBuilder<byte[]> builder = mock(ProducerBuilder.class);
        when(builder.topic(anyString())).thenReturn(builder);
        when(builder.compressionType(any())).thenReturn(builder);
        when(builder.create()).thenReturn(mockProducer);
        when(mockClient.newProducer()).thenReturn(builder);

        // Spy on service and override client creation
        pulsarService = spy(new PulsarService(config));
        doReturn(mockClient).when(pulsarService).createPulsarClient(anyString());

        pulsarService.start(); // now uses mockClient
        pulsarService.newLogProducer(); // uses mock builder

        verify(mockClient).newProducer();
        verify(builder).topic("log-topic");
        verify(builder).compressionType(CompressionType.LZ4);
        verify(builder).create();
    }


}