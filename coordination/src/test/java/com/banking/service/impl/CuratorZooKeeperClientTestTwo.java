package com.banking.service.impl;

import com.banking.registry.service.impl.CuratorZooKeeperClient;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CuratorZooKeeperClient with TestingServer")
class CuratorZooKeeperClientTestTwo {

    private TestingServer testingServer;
    private CuratorZooKeeperClient client;

    @BeforeEach
    void setUp() throws Exception {
        testingServer = new TestingServer(2181, true);
        testingServer.start();

        client = new CuratorZooKeeperClient(testingServer.getConnectString());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (client != null) {
            client.close();
        }
        if (testingServer != null) {
            testingServer.close();
        }
    }

    @Test
    @DisplayName("Should work with TestingServer")
    void shouldWorkWithTestingServer() throws Exception {
        Thread.sleep(500); // Wait for connection

        assertTrue(client.isConnected());

        String path = "/test";
        String data = "test data";

        client.createNode(path, data.getBytes(), false, false);
        assertEquals(data, new String(client.getData(path)));
    }
}