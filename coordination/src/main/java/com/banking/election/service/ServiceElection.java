package com.banking.election.service;

public interface ServiceElection {
    /**
     * Start participating in the leader election.
     */
    void start() throws Exception;

    /**
     * Stop participating in the leader election.
     */
    void close() throws Exception;

    /**
     * Check if this node is currently the leader.
     * @return true if this node is the leader
     */
    boolean isLeader();

    /**
     * Register a listener that will be notified when leadership changes.
     * @param listener the listener to notify
     */
    void addListener(ServiceElectionListener listener);

    /**
     * Unregister a previously registered listener.
     * @param listener the listener to remove
     */
    void removeListener(ServiceElectionListener listener);

    String getCurrentLeader() throws Exception;
}
