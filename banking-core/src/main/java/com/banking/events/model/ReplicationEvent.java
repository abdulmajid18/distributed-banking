package com.banking.events.model;

import com.banking.events.EventType;

import java.util.UUID;

public class ReplicationEvent {
    private final String eventId;
    private final EventType eventType;
    private final String commandId;
    private final long timestamp;
    private final String source;

    protected ReplicationEvent(EventType eventType, String commandId, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.commandId = commandId;
        this.timestamp = System.currentTimeMillis();
        this.source = source;
    }

    // Getters
    public String getEventId() { return eventId; }
    public EventType getEventType() { return eventType; }
    public String getCommandId() { return commandId; }
    public long getTimestamp() { return timestamp; }
    public String getSource() { return source; }
}

