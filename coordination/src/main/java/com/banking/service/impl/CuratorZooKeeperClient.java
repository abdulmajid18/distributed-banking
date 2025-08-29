package com.banking.service.impl;

import com.banking.service.ServiceRegistryClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

public class CuratorZooKeeperClient implements ServiceRegistryClient {

    private final CuratorFramework client;

    public CuratorZooKeeperClient(String connectionString) {
        this.client = CuratorFrameworkFactory.builder()
                .connectString(connectionString)
                .sessionTimeoutMs(60000)
                .connectionTimeoutMs(15000)
                .namespace("Banking")
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();
    }


    @Override
    public void close() {
        client.close();
    }

    @Override
    public boolean isConnected() {
        return client.getZookeeperClient().isConnected();
    }

    @Override
    public void createNode(String path, byte[] data, boolean ephemeral, boolean sequential) throws Exception {
        CreateMode mode;

        if (ephemeral && sequential) {
            mode = CreateMode.EPHEMERAL_SEQUENTIAL;
        } else if (ephemeral) {
            mode = CreateMode.EPHEMERAL;
        } else if (sequential) {
            mode = CreateMode.PERSISTENT_SEQUENTIAL;
        } else {
            mode = CreateMode.PERSISTENT;
        }

        client.create().creatingParentsIfNeeded().withMode(mode).forPath(path, data);
    }

    @Override
    public void deleteNode(String path) throws Exception {
        client.delete().forPath(path);
    }

    @Override
    public boolean exists(String path) throws Exception {
        return client.checkExists().forPath(path) != null;
    }

    @Override
    public byte[] getData(String path) throws Exception {
        return client.getData().forPath(path);
    }

    @Override
    public void setData(String path, byte[] data) throws Exception {
        client.setData().forPath(path, data);
    }

    @Override
    public List<String> getChildren(String path) throws Exception {
        return client.getChildren().forPath(path);
    }

    @Override
    public void watchNode(String path, NodeWatcher watcher) throws Exception {

    }

    @Override
    public void watchChildren(String path, ChildrenWatcher watcher) throws Exception {

    }
}
