package com.banking.discovery.service;


import org.apache.curator.x.discovery.ServiceCacheBuilder;
import org.apache.curator.x.discovery.ServiceProviderBuilder;

import java.util.Collection;

public interface ServiceDiscovery<T> {
    /**
     * Register a service instance in the discovery system.
     * @param instance the service instance to register
     */
    void registerService(ServiceInstance<T> instance) throws Exception;

    /**
     * Unregister a service instance from the discovery system.
     * @param instance the service instance to unregister
     */
    void unregisterService(ServiceInstance<T> instance) throws Exception;

    void updateService(ServiceInstance<T> var1) throws Exception;

    void close() throws Exception;

    Collection<ServiceInstance<T>> queryForInstances(String name) throws Exception;

    ServiceInstance<T> queryForInstance(String name, String id) throws Exception;

    Collection<String> queryForNames() throws Exception;

    ServiceCacheBuilder<T> serviceCacheBuilder();

    ServiceProviderBuilder<T> serviceProviderBuilder();
}
