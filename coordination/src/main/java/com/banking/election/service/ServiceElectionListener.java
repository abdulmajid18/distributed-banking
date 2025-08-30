package com.banking.election.service;

public interface ServiceElectionListener {
    /**
     * Called when this node becomes the leader.
     */
    void onElectedLeader();

    /**
     * Called when this node loses leadership.
     */
    void onRevokedLeadership();
}
