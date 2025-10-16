package com.bank.replication;

public interface ReplicationStrategy {
    void replicate(byte[] data);
}
