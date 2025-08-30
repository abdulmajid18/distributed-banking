package com.banking.registry.service;

import java.util.List;

public interface ServiceRegistryClient {
    void close();

    boolean isConnected();

    void createNode(String path, byte[] data, boolean ephemeral, boolean sequential) throws Exception;

    void deleteNode(String path) throws Exception;

    boolean exists(String path) throws Exception;

    byte[] getData(String path) throws Exception;

    void setData(String path, byte[] data) throws Exception;

    List<String> getChildren(String path) throws Exception;

    void watchNode(String path, NodeWatcher watcher) throws Exception;

    void watchChildren(String path, ChildrenWatcher watcher) throws Exception;

    interface NodeWatcher {
        void nodeChanged(String path, byte[] newData);
    }

    interface ChildrenWatcher {
        void childrenChanged(String path, List<String> children);
    }

}
