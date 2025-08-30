package com.banking.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DisplayName("CuratorZooKeeperClient Integration Tests")
class CuratorZooKeeperClientTest {

    private static final int ZOOKEEPER_PORT = 2181;

    @Container
    private static final GenericContainer<?> zookeeperContainer =
            new GenericContainer<>("zookeeper:latest")
                    .withExposedPorts(ZOOKEEPER_PORT)
                    .withEnv("ZOO_MY_ID", "1")
                    .withEnv("ZOOKEEPER_CLIENT_PORT", "2181");

    private CuratorZooKeeperClient client;

    private String connectionString;

    @BeforeEach
    void setUp() {
        connectionString = String.format("%s:%d",
                zookeeperContainer.getHost(),
                zookeeperContainer.getMappedPort(ZOOKEEPER_PORT));

        client = new CuratorZooKeeperClient(connectionString);
    }
    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
    }


    @Test
    @DisplayName("Should connect to ZooKeeper container")
    void shouldConnectToZooKeeper() throws InterruptedException {
        Thread.sleep(2000);

        assertTrue(client.isConnected(), "Client should be connected to ZooKeeper");
    }

    @Test
    @DisplayName("Should create and read ephemeral node")
    void shouldCreateAndReadEphemeralNode() throws Exception {
        String path = "/test-ephemeral";
        String data = "ephemeral data";

        client.createNode(path, data.getBytes(), true, false);

        assertTrue(client.exists(path), "Ephemeral node should exist");
        assertEquals(data, new String(client.getData(path)), "Data should match");
    }

    @Test
    @DisplayName("Should create node with parent hierarchy")
    void shouldCreateNodeWithParentHierarchy() throws Exception {
        String deepPath = "/level1/level2/level3/deep-node";
        String data = "deep data";

        client.createNode(deepPath, data.getBytes(), false, false);

        assertTrue(client.exists(deepPath));
        assertEquals(data, new String(client.getData(deepPath)));
    }

    @Test
    @DisplayName("Should handle non-existent node checks")
    void shouldHandleNonExistentNode() throws Exception {
        String nonExistentPath = "/non-existent-path";

        assertFalse(client.exists(nonExistentPath));
    }

    @Test
    @DisplayName("Should delete node")
    void shouldDeleteNode() throws Exception {
        String path = "/test-delete";
        String data = "data to delete";

        client.createNode(path, data.getBytes(), false, false);
        assertTrue(client.exists(path));

        client.deleteNode(path);
        assertFalse(client.exists(path));
    }

    @Test
    @DisplayName("Should update node data")
    void shouldUpdateNodeData() throws Exception {
        String path = "/test-update";
        String initialData = "initial data";
        String updatedData = "updated data";

        client.createNode(path, initialData.getBytes(), false, false);
        assertEquals(initialData, new String(client.getData(path)));

        client.setData(path, updatedData.getBytes());
        assertEquals(updatedData, new String(client.getData(path)));
    }

    @Test
    @DisplayName("Should create sequential node")
    void shouldCreateSequentialNode() throws Exception {
        String path = "/test-sequential";
        String data = "sequential data";

        client.createNode(path, data.getBytes(), false, true);

        // Sequential nodes get a suffix like /test-sequential0000000000
        List<String> children = client.getChildren("/");
        boolean foundSequential = children.stream()
                .anyMatch(child -> child.startsWith("test-sequential"));

        assertTrue(foundSequential, "Should find sequential node");
    }
}