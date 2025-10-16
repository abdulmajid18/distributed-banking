package com.banking.events.serializer;

import com.banking.dto.CreateAccountDto;
import com.banking.events.AccountCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class AccountCreatedEventSerializer {
    private final ObjectMapper objectMapper;
    private final String sourceNode; // e.g., "primary", "node-1", etc.

    public AccountCreatedEventSerializer(String sourceNode) {
        this.objectMapper = new ObjectMapper();
        this.sourceNode = sourceNode;
    }

    public byte[] serializeAccountCreated(CreateAccountDto accountDto, String common) throws JsonProcessingException {
        byte[] commandData = objectMapper.writeValueAsBytes(accountDto);

        AccountCreatedEvent event = new AccountCreatedEvent("commandId",
                this.sourceNode,
                commandData,
                accountDto
        );
        return objectMapper.writeValueAsBytes(event);
    }

}

