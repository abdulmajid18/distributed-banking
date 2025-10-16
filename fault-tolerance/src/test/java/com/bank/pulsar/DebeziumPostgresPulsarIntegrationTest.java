package com.bank.pulsar;

import org.apache.pulsar.client.api.*;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class DebeziumPostgresPulsarIntegrationTest {

    private static final Network network = Network.newNetwork();

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:13"))
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withNetwork(network)
            .withNetworkAliases("postgres")
            .withCommand("postgres",
                    "-c", "wal_level=logical",
                    "-c", "max_wal_senders=4",
                    "-c", "max_replication_slots=4");

    private static final PulsarContainer pulsar = new PulsarContainer(
            DockerImageName.parse("apachepulsar/pulsar:3.0.0"))
            .withFunctionsWorker()
            .withNetwork(network)
            .withNetworkAliases("pulsar");

    private Connection postgresConnection;
    private PulsarClient pulsarClient;

    @BeforeAll
    static void setupAll() throws Exception {
        postgres.start();
        pulsar.start();

        Thread.sleep(5000);
        setupPostgresLogicalReplication();
        Thread.sleep(5000);
        downloadConnector();
        Thread.sleep(5000);
        deployDebeziumConnector();
        Thread.sleep(30000); // Increased wait for connector startup
    }

    @AfterAll
    static void tearDownAll() {
        if (pulsar != null) pulsar.stop();
        if (postgres != null) postgres.stop();
        if (network != null) network.close();
    }

    @BeforeEach
    void setUp() throws Exception {
        postgresConnection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );

        pulsarClient = PulsarClient.builder()
                .serviceUrl(pulsar.getPulsarBrokerUrl())
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (postgresConnection != null && !postgresConnection.isClosed()) {
            postgresConnection.close();
        }
        if (pulsarClient != null) {
            pulsarClient.close();
        }
    }

    private static void setupPostgresLogicalReplication() throws Exception {
        System.out.println("Setting up PostgreSQL logical replication...");

        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword())) {

            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE PUBLICATION debezium_pub FOR ALL TABLES;");
                conn.commit();
                System.out.println("Publication created successfully");
            } catch (Exception e) {
                conn.rollback();
                System.out.println("Publication might already exist: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static void downloadConnector() throws Exception {
        System.out.println("Downloading Debezium PostgreSQL connector (NAR format)...");

        Container.ExecResult result = pulsar.execInContainer("bash", "-c",
                "mkdir -p /tmp/connectors && " +
                        "cd /tmp/connectors && " +
                        "wget -q https://archive.apache.org/dist/pulsar/pulsar-3.0.0/connectors/pulsar-io-debezium-postgres-3.0.0.nar && " +
                        "ls -la /tmp/connectors/"
        );

        System.out.println("Download result: " + result.getStdout());
        if (result.getExitCode() != 0) {
            System.err.println("Download error: " + result.getStderr());
            throw new Exception("Failed to download connector");
        }
    }

    private static void deployDebeziumConnector() throws Exception {
        System.out.println("Deploying Debezium PostgreSQL connector...");

        // Fixed YAML configuration with proper formatting and required fields
        String connectorConfigYaml = """
                tenant: "public"
                namespace: "default"
                name: "debezium-postgres-source"
                topicName: "persistent://public/default/debezium-postgres-events"
                archive: "/tmp/connectors/pulsar-io-debezium-postgres-3.0.0.nar"
                parallelism: 1
                autoAck: true
                
                configs:
                  database.hostname: "postgres"
                  database.port: "5432"
                  database.user: "testuser"
                  database.password: "testpass"
                  database.dbname: "testdb"
                  database.server.name: "dbserver1"
                  plugin.name: "pgoutput"
                  publication.name: "debezium_pub"
                  table.include.list: "public.*"
                  decimal.handling.mode: "string"
                  include.unknown.datatypes: "false"
                """;

        pulsar.execInContainer("bash", "-c", "mkdir -p /tmp/connectors");

        pulsar.execInContainer("bash", "-c",
                "cat > /tmp/connectors/debezium-postgres-source-config.yaml << 'EOF'\n" +
                        connectorConfigYaml + "\nEOF");

        // Deploy using the YAML configuration file
        Container.ExecResult deployResult = pulsar.execInContainer(
                "bash", "-c",
                "bin/pulsar-admin sources create --source-config-file /tmp/connectors/debezium-postgres-source-config.yaml"
        );

        if (deployResult.getExitCode() != 0) {
            System.err.println("Deployment failed: " + deployResult.getStderr());
            throw new Exception("Failed to deploy connector: " + deployResult.getStderr());
        }

        System.out.println("Deployment result: " + deployResult.getStdout());

        Thread.sleep(15000);

        // Check connector status
        Container.ExecResult statusResult = pulsar.execInContainer(
                "bin/pulsar-admin", "sources", "status",
                "--name", "debezium-postgres-source",
                "--tenant", "public",
                "--namespace", "default"
        );
        System.out.println("Connector status: " + statusResult.getStdout());

        if (statusResult.getStdout().contains("\"running\" : false")) {
            System.err.println("WARNING: Connector failed to start. Checking logs...");
            Container.ExecResult logsResult = pulsar.execInContainer(
                    "bash", "-c",
                    "tail -100 /var/log/pulsar/pulsar.log || echo 'Log file not found'"
            );
            System.err.println("Pulsar logs: " + logsResult.getStdout());
        }
    }

    @Test
    void testDebeziumCapturesInventoryTableChanges() throws Exception {
        // Create inventory schema and table
        try (Statement stmt = postgresConnection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS public.inventory CASCADE;");
            stmt.execute(
                    "CREATE TABLE public.inventory (" +
                            "id SERIAL PRIMARY KEY, " +
                            "name VARCHAR(255) NOT NULL, " +
                            "quantity INT DEFAULT 0);"
            );
        }

        System.out.println("Table created, waiting for connector to detect schema...");
        Thread.sleep(20000);

        // Insert test data
        try (Statement stmt = postgresConnection.createStatement()) {
            stmt.execute(
                    "INSERT INTO public.inventory (name, quantity) " +
                            "VALUES ('widget', 100);"
            );
        }

        System.out.println("Data inserted, waiting to consume from Pulsar...");
        Thread.sleep(10000);

        // Consume from the output topic
        String topic = "persistent://public/default/debezium-postgres-events";

        System.out.println("Attempting to consume from topic: " + topic);

        try (Consumer<byte[]> consumer = pulsarClient.newConsumer()
                .topic(topic)
                .subscriptionName("test-subscription")
                .subscriptionType(SubscriptionType.Exclusive)
                .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
                .subscribe()) {

            Message<byte[]> message = consumer.receive(30, TimeUnit.SECONDS);
            assertThat(message)
                    .as("Should receive CDC event from Pulsar")
                    .isNotNull();

            String content = new String(message.getData());
            System.out.println("Received CDC event: " + content);

            assertThat(content)
                    .as("CDC event should contain database server name")
                    .contains("dbserver1");

            consumer.acknowledge(message);
        }
    }

    @Test
    void testDebeziumCapturesMultipleOperations() throws Exception {
        // Create test table
        try (Statement stmt = postgresConnection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS public.products CASCADE;");
            stmt.execute(
                    "CREATE TABLE public.products (" +
                            "id SERIAL PRIMARY KEY, " +
                            "name VARCHAR(255) NOT NULL, " +
                            "price DECIMAL(10, 2));"
            );
        }

        Thread.sleep(20000);

        // Perform multiple operations: INSERT, UPDATE, DELETE
        try (Statement stmt = postgresConnection.createStatement()) {
            stmt.execute("INSERT INTO public.products (name, price) VALUES ('Product1', 10.50);");
            stmt.execute("INSERT INTO public.products (name, price) VALUES ('Product2', 20.75);");
            stmt.execute("UPDATE public.products SET price = 15.00 WHERE name = 'Product1';");
            stmt.execute("DELETE FROM public.products WHERE name = 'Product2';");
        }

        System.out.println("Multiple operations executed, waiting for events...");
        Thread.sleep(15000);

        String topic = "persistent://public/default/debezium-postgres-source-debezium-offset-topic";

        try (Consumer<byte[]> consumer = pulsarClient.newConsumer()
                .topic(topic)
                .subscriptionName("multi-ops-subscription")
                .subscriptionType(SubscriptionType.Exclusive)
                .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
                .subscribe()) {

            int eventCount = 0;
            Message<byte[]> message;

            while ((message = consumer.receive(15, TimeUnit.SECONDS)) != null && eventCount < 10) {
                String content = new String(message.getData());
                System.out.println("Event " + (eventCount + 1) + ": " + content);
                consumer.acknowledge(message);
                eventCount++;
            }

            assertThat(eventCount)
                    .as("Should receive CDC events for operations")
                    .isGreaterThan(0);
        }
    }

    @Test
    void testConnectorHealthCheck() throws Exception {
        Container.ExecResult listResult = pulsar.execInContainer(
                "bin/pulsar-admin", "sources", "list",
                "--tenant", "public",
                "--namespace", "default"
        );

        System.out.println("Running sources: " + listResult.getStdout());
        assertThat(listResult.getStdout())
                .as("Connector should be listed")
                .contains("debezium-postgres-source");

        Container.ExecResult statusResult = pulsar.execInContainer(
                "bin/pulsar-admin", "sources", "status",
                "--name", "debezium-postgres-source",
                "--tenant", "public",
                "--namespace", "default"
        );

        System.out.println("Connector status details: " + statusResult.getStdout());
        assertThat(statusResult.getStdout())
                .as("Connector should be running")
                .contains("\"running\" : true");
    }

    @Test
    void testDiscoverTopics() throws Exception {
        // List all topics to see what's available
        Container.ExecResult result = pulsar.execInContainer(
                "bin/pulsar-admin", "topics", "list", "public/default"
        );

        System.out.println("Available topics: " + result.getStdout());

        // Check the connector's actual output topic
        Container.ExecResult connectorStatus = pulsar.execInContainer(
                "bin/pulsar-admin", "sources", "status",
                "--name", "debezium-postgres-source",
                "--tenant", "public",
                "--namespace", "default"
        );

        System.out.println("Connector status with topic info: " + connectorStatus.getStdout());
    }
}