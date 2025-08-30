package com.banking.discovery.service.impl;

import com.banking.discovery.service.ServiceDiscovery;
import com.banking.discovery.service.ServiceInstance;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceCacheBuilder;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceProviderBuilder;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Collectors;

public class CuratorServiceDiscovery<T> implements ServiceDiscovery<T> {

    private static final Logger log = LoggerFactory.getLogger(CuratorServiceDiscovery.class);

    private final org.apache.curator.x.discovery.ServiceDiscovery<T>  curatorDiscovery;

    public CuratorServiceDiscovery(CuratorFramework client, String basePath, Class<T> payloadClass) throws Exception {
        log.info("Initializing CuratorServiceDiscovery with basePath='{}' and payloadClass={}", basePath, payloadClass.getSimpleName());

        this.curatorDiscovery = ServiceDiscoveryBuilder.builder(payloadClass)
                .client(client)
                .basePath(basePath)
                .serializer(new JsonInstanceSerializer<>(payloadClass))
                .build();

        this.curatorDiscovery.start();
        log.info("CuratorServiceDiscovery started successfully at path '{}'", basePath);
    }


    @Override
    public void registerService(ServiceInstance<T> instance) throws Exception {
        org.apache.curator.x.discovery.ServiceInstance<T> curatorInstance = convertToCuratorInstance(instance);
        curatorDiscovery.registerService(curatorInstance);
    }

    @Override
    public void unregisterService(ServiceInstance<T> instance) throws Exception {
        org.apache.curator.x.discovery.ServiceInstance<T> curatorInstance = convertToCuratorInstance(instance);

        curatorDiscovery.unregisterService(curatorInstance);
    }

    @Override
    public void updateService(ServiceInstance<T> var1) throws Exception {

    }

    private org.apache.curator.x.discovery.ServiceInstance<T> convertToCuratorInstance(ServiceInstance<T> instance) throws Exception {
        return org.apache.curator.x.discovery.ServiceInstance.<T>builder()
                .name(instance.getName())
                .id(instance.getId())
                .address(instance.getAddress())
                .port(instance.getPort())
                .payload(instance.getPayload())
                .build();
    }

    private ServiceInstance<T> convertFromCuratorInstance(org.apache.curator.x.discovery.ServiceInstance<T> curatorInstance) {
        return new ServiceInstance<>(
                curatorInstance.getName(),
                curatorInstance.getId(),
                curatorInstance.getAddress(),
                curatorInstance.getPort(),
                curatorInstance.getPayload()
        );
    }

    @Override
    public void close() throws Exception {
        curatorDiscovery.close();
    }

    @Override
    public Collection<ServiceInstance<T>> queryForInstances(String name) throws Exception {
        Collection<org.apache.curator.x.discovery.ServiceInstance<T>> instances =
                curatorDiscovery.queryForInstances(name);
        return instances.stream()
                .map(this::convertFromCuratorInstance)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceInstance<T> queryForInstance(String name, String id) throws Exception {
        org.apache.curator.x.discovery.ServiceInstance<T> instance =
                curatorDiscovery.queryForInstance(name, id);

        return convertFromCuratorInstance(instance);
    }

    @Override
    public Collection<String> queryForNames() throws Exception {
        return curatorDiscovery.queryForNames();
    }

    @Override
    public ServiceCacheBuilder<T> serviceCacheBuilder() {
        return null;
    }

    @Override
    public ServiceProviderBuilder<T> serviceProviderBuilder() {
        return null;
    }
}
