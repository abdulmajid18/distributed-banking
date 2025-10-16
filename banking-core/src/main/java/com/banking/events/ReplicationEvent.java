package com.banking.events;

import java.util.UUID;

public abstract class ReplicationEvent {
    private final String eventId;
    private final EventType eventType;
    private final String commandId; // Correlation ID for tracking
    private final long timestamp;
    private final String source;
    private final byte[] commandData; // Serialized DTO

    public ReplicationEvent(EventType eventType, String commandId, String source, byte[] commandData) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.commandId = commandId;
        this.timestamp = System.currentTimeMillis();
        this.source = source;
        this.commandData = commandData;
    }

    // Getters
    public String getEventId() { return eventId; }
    public EventType getEventType() { return eventType; }
    public String getCommandId() { return commandId; }
    public long getTimestamp() { return timestamp; }
    public String getSource() { return source; }
    public byte[] getCommandData() { return commandData; }
}