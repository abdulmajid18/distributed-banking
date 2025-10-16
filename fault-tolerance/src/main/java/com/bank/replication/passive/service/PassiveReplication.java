package com.bank.replication.passive.service;


import com.bank.pulsar.MessageService;
import com.bank.pulsar.PulsarService;
import com.bank.pulsar.exception.MessageServiceException;
import com.bank.replication.ReplicationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PassiveReplication implements ReplicationStrategy {
    private final MessageService messageService;
    private static final Logger logger = LoggerFactory.getLogger(PulsarService.class);


    public PassiveReplication(MessageService messageService) {
        this.messageService = messageService;
    }

    private void addLogEntry(byte[] logData) {
        try {
            messageService.sendLogMessage(logData);
        } catch (MessageServiceException e) {
            // Handle exception appropriately
            logger.info("Failed to add log entry: {}", e.getMessage());
        }
    }

    @Override
    public void replicate(byte[] data) {
        // Example replication logic
        addLogEntry(data);
    }
}