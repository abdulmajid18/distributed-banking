package com.banking.discovery.service;

public class ServiceInstance<T> {
    private final String name;
    private final String id;
    private final String address;
    private final int port;
    private final T payload;

    public ServiceInstance(String name, String id, String address, int port, T payload) {
        this.name = name;
        this.id = id;
        this.address = address;
        this.port = port;
        this.payload = payload;
    }

    public String getName() { return name; }
    public String getId() { return id; }
    public String getAddress() { return address; }
    public int getPort() { return port; }
    public T getPayload() { return payload; }
}
