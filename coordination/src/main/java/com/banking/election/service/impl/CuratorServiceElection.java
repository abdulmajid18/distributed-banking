package com.banking.election.service.impl;

import com.banking.election.service.ServiceElection;
import com.banking.election.service.ServiceElectionListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CuratorServiceElection implements ServiceElection {
    private final List<ServiceElectionListener> listeners = new CopyOnWriteArrayList<>();
    private final LeaderLatch leaderLatch;
    private final String nodeId;
    private static final Logger logger = LoggerFactory.getLogger(CuratorServiceElection.class);
    private volatile boolean closed = false;

    private final LeaderLatchListener internalListener = new LeaderLatchListener() {
        @Override
        public void isLeader() {
            logger.info("Node {} is now the LEADER", nodeId);
            notifyListeners(true);
            performLeaderTasks();
        }

        @Override
        public void notLeader() {
            logger.info("Node {} is no longer the leader", nodeId);
            notifyListeners(false);
            stopLeaderTasks();
        }
    };

    public CuratorServiceElection(CuratorFramework client, String latchPath, String nodeId) {
        this.nodeId = nodeId;
        this.leaderLatch = new LeaderLatch(client, latchPath, nodeId);
    }

    @Override
    public void start() throws Exception {
        leaderLatch.addListener(internalListener);
        leaderLatch.start();
    }

    private void notifyListeners(boolean isLeader) {
        for (ServiceElectionListener listener : listeners) {
            try {
                if (isLeader) {
                    listener.onElectedLeader();
                } else {
                    listener.onRevokedLeadership();
                }
            } catch (Exception e) {
                logger.error("Error notifying listener {}", listener.getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (closed) return;
        closed = true;

        try {
            leaderLatch.removeListener(internalListener);
            leaderLatch.close();
        } catch (Exception e) {
            logger.warn("Error closing leader latch", e);
            throw e;
        }
    }

    @Override
    public boolean isLeader() {
        return !closed && leaderLatch.hasLeadership();
    }

    @Override
    public void addListener(ServiceElectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ServiceElectionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String getCurrentLeader() throws Exception {
        if (closed) throw new IllegalStateException("Service election closed");
        return leaderLatch.getLeader().getId();
    }

    private void performLeaderTasks() {
        try {
            logger.debug("Performing leader tasks for node {}", nodeId);
        } catch (Exception e) {
            logger.error("Error performing leader tasks", e);
        }
    }

    private void stopLeaderTasks() {
        try {
            logger.debug("Stopping leader tasks for node {}", nodeId);
        } catch (Exception e) {
            logger.error("Error stopping leader tasks", e);
        }
    }
    public String getNodeId() {
        return nodeId;
    }

}
