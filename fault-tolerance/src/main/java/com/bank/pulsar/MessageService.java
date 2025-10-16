package com.bank.pulsar;

import com.bank.pulsar.exception.MessageServiceException;

public interface MessageService {
    void sendLogMessage(byte[] message) throws MessageServiceException;
    void start() throws MessageServiceException;
    void close() throws MessageServiceException;
}
