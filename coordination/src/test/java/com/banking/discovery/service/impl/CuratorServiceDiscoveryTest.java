package com.banking.discovery.service.impl;

import com.banking.discovery.service.ServiceInstance;
import com.banking.registry.service.impl.CuratorZooKeeperClient;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
@DisplayName("CuratorServiceDiscovery Integration Tests")
class CuratorServiceDiscoveryTest {

    private static final int ZOOKEEPER_PORT = 2181;

    @Container
    private static final GenericContainer<?> zookeeperContainer =
            new GenericContainer<>("zookeeper:latest")
                    .withExposedPorts(ZOOKEEPER_PORT)
                    .withEnv("ZOO_MY_ID", "1")
                    .withEnv("ZOOKEEPER_CLIENT_PORT", "2181");

    private CuratorZooKeeperClient curatorClient;

    private String connectionString;

    private CuratorServiceDiscovery<ServiceMetadata> serviceDiscovery;



    @BeforeEach
    void setUp() throws Exception {
        connectionString = String.format("%s:%d",
                zookeeperContainer.getHost(),
                zookeeperContainer.getMappedPort(ZOOKEEPER_PORT));

        curatorClient = new CuratorZooKeeperClient(connectionString);
        curatorClient.getClient().blockUntilConnected();
        serviceDiscovery = new CuratorServiceDiscovery<ServiceMetadata>(
                curatorClient.getClient(), "/services", ServiceMetadata.class
        );
    }
    @AfterEach
    void tearDown() throws Exception {
        if (serviceDiscovery != null) {
            serviceDiscovery.close();
        }
        if (curatorClient != null) {
            curatorClient.close();
        }
    }

    private ServiceInstance<ServiceMetadata> createTestInstance(String serviceName, String instanceId) {
        ServiceMetadata metadata = new ServiceMetadata(
                "test",
                "1.0.0",
                Map.of("region", "us-east-1", "zone", "a")
        );

        return new ServiceInstance<>(
                serviceName,
                instanceId,
                "localhost",
                8080,
                metadata
        );
    }


    @Test
    @DisplayName("Should register service instance successfully")
    void shouldRegisterServiceInstance() throws Exception {
        ServiceInstance<ServiceMetadata> instance = createTestInstance("payment-service", "instance-1");
        serviceDiscovery.registerService(instance);
        Collection<ServiceInstance<ServiceMetadata>> instances =
                serviceDiscovery.queryForInstances("payment-service");

        assertEquals(1, instances.size());
        ServiceInstance<ServiceMetadata> registeredInstance = instances.iterator().next();

        assertEquals("payment-service", registeredInstance.getName());
        assertEquals("instance-1", registeredInstance.getId());
        assertEquals("localhost", registeredInstance.getAddress());
        assertEquals(8080, registeredInstance.getPort());
        assertEquals(instance.getPayload(), registeredInstance.getPayload());
    }

    @Test
    @DisplayName("Should unregister service instance")
    void shouldUnregisterServiceInstance() throws Exception {
        ServiceInstance<ServiceMetadata> instance = createTestInstance("auth-service", "instance-1");

        serviceDiscovery.registerService(instance);
        assertEquals(1, serviceDiscovery.queryForInstances("auth-service").size());

        serviceDiscovery.unregisterService(instance);

        Collection<ServiceInstance<ServiceMetadata>> instances =
                serviceDiscovery.queryForInstances("auth-service");

        assertTrue(instances.isEmpty());
    }
    @Test
    @DisplayName("Should query for specific service instance by ID")
    void shouldQueryForSpecificInstance() throws Exception {
        ServiceInstance<ServiceMetadata> instance1 = createTestInstance("user-service", "instance-1");
        ServiceInstance<ServiceMetadata> instance2 = createTestInstance("user-service", "instance-2");

        serviceDiscovery.registerService(instance1);
        serviceDiscovery.registerService(instance2);

        // Query specific instance
        ServiceInstance<ServiceMetadata> foundInstance =
                serviceDiscovery.queryForInstance("user-service", "instance-1");

        assertNotNull(foundInstance);
        assertEquals("instance-1", foundInstance.getId());
        assertEquals("user-service", foundInstance.getName());
    }

    @Test
    @DisplayName("Should return empty collection for non-existent service")
    void shouldReturnEmptyForNonExistentService() throws Exception {
        Collection<ServiceInstance<ServiceMetadata>> instances =
                serviceDiscovery.queryForInstances("non-existent-service");

        assertTrue(instances.isEmpty());
    }

    @Test
    @DisplayName("Should return null for non-existent instance")
    void shouldReturnNullForNonExistentInstance() throws Exception {
        ServiceInstance<ServiceMetadata> instance =
                serviceDiscovery.queryForInstance("user-service", "non-existent-id");

        assertNull(instance);
    }

    @Test
    @DisplayName("Should query for all service names")
    void shouldQueryForServiceNames() throws Exception {
        serviceDiscovery.registerService(createTestInstance("service-a", "instance-1"));
        serviceDiscovery.registerService(createTestInstance("service-b", "instance-1"));
        serviceDiscovery.registerService(createTestInstance("service-c", "instance-1"));

        Collection<String> serviceNames = serviceDiscovery.queryForNames();

        assertTrue(serviceNames.containsAll(List.of("service-a", "service-b", "service-c")));
        assertEquals(3, serviceNames.size());
    }

    @Test
    @DisplayName("Should handle multiple instances of same service")
    void shouldHandleMultipleInstancesOfSameService() throws Exception {
        ServiceInstance<ServiceMetadata> instance1 = createTestInstance("inventory-service", "instance-1");
        ServiceInstance<ServiceMetadata> instance2 = createTestInstance("inventory-service", "instance-2");
        ServiceInstance<ServiceMetadata> instance3 = createTestInstance("inventory-service", "instance-3");

        serviceDiscovery.registerService(instance1);
        serviceDiscovery.registerService(instance2);
        serviceDiscovery.registerService(instance3);

        Collection<ServiceInstance<ServiceMetadata>> instances =
                serviceDiscovery.queryForInstances("inventory-service");

        assertEquals(3, instances.size());

        // Verify all instances have correct service name
        assertTrue(instances.stream()
                .allMatch(instance -> "inventory-service".equals(instance.getName())));
    }
    @Test
    @DisplayName("Should preserve metadata during registration and query")
    void shouldPreserveMetadata() throws Exception {
        ServiceMetadata originalMetadata = new ServiceMetadata(
                "production",
                "2.1.0",
                Map.of("region", "eu-west-1", "environment", "prod", "team", "backend")
        );

        ServiceInstance<ServiceMetadata> instance = new ServiceInstance<>(
                "metadata-service",
                "test-instance",
                "server.example.com",
                9090,
                originalMetadata
        );

        serviceDiscovery.registerService(instance);

        ServiceInstance<ServiceMetadata> queriedInstance =
                serviceDiscovery.queryForInstance("metadata-service", "test-instance");

        assertNotNull(queriedInstance);
        assertEquals(originalMetadata, queriedInstance.getPayload());
        assertEquals("production", queriedInstance.getPayload().getEnvironment());
        assertEquals("2.1.0", queriedInstance.getPayload().getVersion());
        assertEquals("eu-west-1", queriedInstance.getPayload().getTags().get("region"));
    }

    @Test
    @DisplayName("Should handle service discovery after restart")
    void shouldHandleServiceDiscoveryAfterRestart() throws Exception {
        // Simulate multiple services running
        ServiceInstance<ServiceMetadata> service1 = createTestInstance("payment-service", "payment-1");
        ServiceInstance<ServiceMetadata> service2 = createTestInstance("auth-service", "auth-1");
        ServiceInstance<ServiceMetadata> service3 = createTestInstance("user-service", "user-1");

        // Register all services
        serviceDiscovery.registerService(service1);
        serviceDiscovery.registerService(service2);
        serviceDiscovery.registerService(service3);

        assertEquals(3, serviceDiscovery.queryForNames().size());

        // Simulate service discovery restart (but keep ZooKeeper running)
        serviceDiscovery.close();

        // Create new service discovery instance (simulating updated deployment)
        CuratorServiceDiscovery<ServiceMetadata> newDiscovery = new CuratorServiceDiscovery<>(
                curatorClient.getClient(),  // Same client maintains ZooKeeper connection
                "/services",
                ServiceMetadata.class
        );

        // Should still see all services because ZooKeeper maintains the state
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Collection<String> serviceNames = newDiscovery.queryForNames();
            assertEquals(3, serviceNames.size());
            assertTrue(serviceNames.containsAll(List.of("payment-service", "auth-service", "user-service")));
        });

        newDiscovery.close();
    }

    @Test
    @DisplayName("Should be able to close multiple times safely")
    void shouldCloseSafelyMultipleTimes() throws Exception {
        ServiceInstance<ServiceMetadata> instance = createTestInstance("test-service", "instance-1");
        serviceDiscovery.registerService(instance);

        // Multiple close calls should not throw exceptions
        serviceDiscovery.close();
        serviceDiscovery.close(); // Second call should be no-op

        assertTrue(true); // If we get here, test passed
    }

    @Test
    @DisplayName("Should handle concurrent registration attempts")
    void shouldHandleConcurrentRegistration() throws Exception {
        ServiceInstance<ServiceMetadata> instance = createTestInstance("concurrent-service", "instance-1");

        // Create multiple service discovery instances sharing same Curator client
        CuratorServiceDiscovery<ServiceMetadata> discovery1 = new CuratorServiceDiscovery<>(
                curatorClient.getClient(), "/services", ServiceMetadata.class);

        CuratorServiceDiscovery<ServiceMetadata> discovery2 = new CuratorServiceDiscovery<>(
                curatorClient.getClient(), "/services", ServiceMetadata.class);

        // Register from both (should work due to ZooKeeper's consistency)
        discovery1.registerService(instance);
        discovery2.registerService(instance); // Same instance, should be idempotent

        // Should only have one instance registered
        Collection<ServiceInstance<ServiceMetadata>> instances =
                discovery1.queryForInstances("concurrent-service");

        assertEquals(1, instances.size());

        discovery1.close();
        discovery2.close();
    }
}