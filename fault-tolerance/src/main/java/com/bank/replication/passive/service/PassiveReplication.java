package com.bank.replication.passive.service;


import com.bank.replication.ReplicationStrategy;

public class PassiveReplication implements ReplicationStrategy {

    public PassiveReplication() {
    }

    private void addLogEntry() {

    }

    @Override
    public void replicate() {
        addLogEntry();
    }
}