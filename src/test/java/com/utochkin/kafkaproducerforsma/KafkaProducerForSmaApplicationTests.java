package com.utochkin.kafkaproducerforsma;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class KafkaProducerForSmaApplicationTests {
    private static final int PORT_DB = 5432;
    private static final int PORT_PRODUCER = 8080;
    private static final int PORT_CONSUMER = 8081;
    private static final int PORT_REDIS = 6379;
    private static final int PORT_MINIO1 = 9000;
    private static final int PORT_MINIO2 = 9090;
    private static final int PORT_ZOOKEEPER = 2181;
    private static final int PORT_KAFKA = 9092;
    private static final String DATABASE_NAME = "SMA";
    private static final String DATABASE_USERNAME = "postgres";
    private static final String DATABASE_PASSWORD = "1234";
    private static final Network CLOUD_NETWORK = Network.newNetwork();
    @Container
    private final static PostgreSQLContainer<?> databaseContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres"))
            .withNetwork(CLOUD_NETWORK)
            .withExposedPorts(PORT_DB)
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_USERNAME)
            .withPassword(DATABASE_PASSWORD)
            .withEnv(Map.of("TZ", "Europe/Moscow"));

    @Container
    private final static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis"))
            .withNetwork(CLOUD_NETWORK)
            .withExposedPorts(PORT_REDIS)
            .withCommand("redis-server --save 20 1 --loglevel warning --requirepass MjEzNDU2N3F3ZXJ0eWRmZ2RmZ2hzZGE")
            .withEnv(Map.of("TZ", "Europe/Moscow"));

    @Container
    private final static GenericContainer<?> minioContainer = new GenericContainer<>(DockerImageName.parse("minio/minio"))
            .withNetwork(CLOUD_NETWORK)
            .withExposedPorts(PORT_MINIO1, PORT_MINIO2)
            .withEnv(Map.of("MINIO_ROOT_USER", "minioadmin"))
            .withEnv(Map.of("MINIO_ROOT_PASSWORD", "minioadmin"))
            .withCommand("minio server /data/minio --console-address ':9090'")
            .withEnv(Map.of("TZ", "Europe/Moscow"));

    @Container
    private final static GenericContainer<?> zookeeperContainer = new GenericContainer<>(DockerImageName.parse("zookeeper"))
            .withNetwork(CLOUD_NETWORK)
            .withExposedPorts(PORT_ZOOKEEPER)
            .withEnv(Map.of("TZ", "Europe/Moscow"));

    @Container
    private final static GenericContainer<?> kafkaContainer = new GenericContainer<>(DockerImageName.parse("obsidiandynamics/kafka"))
            .withNetwork(CLOUD_NETWORK)
            .withExposedPorts(PORT_KAFKA)
            .withEnv(Map.of("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "DOCKER_INTERNAL:PLAINTEXT,DOCKER_EXTERNAL:PLAINTEXT"))
            .withEnv(Map.of("KAFKA_LISTENERS", "DOCKER_INTERNAL://:29092,DOCKER_EXTERNAL://:9092"))
            .withEnv(Map.of("KAFKA_ADVERTISED_LISTENERS", "DOCKER_INTERNAL://kafka:29092,DOCKER_EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:9092"))
            .withEnv(Map.of("KAFKA_INTER_BROKER_LISTENER_NAME", "DOCKER_INTERNAL"))
            .withEnv(Map.of("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181"))
            .withEnv(Map.of("KAFKA_BROKER_ID", "1"))
            .withEnv(Map.of("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1"))
            .withEnv(Map.of("TZ", "Europe/Moscow"))
            .dependsOn(zookeeperContainer);
    @Container
    private final static GenericContainer<?> producerContainer = new GenericContainer<>(DockerImageName.parse("sma-producer_app"))
            .withNetwork(CLOUD_NETWORK)
            .withExposedPorts(PORT_PRODUCER)
            .withEnv(Map.of("SPRING_DATASOURCE_URL", "jdbc:postgresql://db:" + PORT_DB + "/" + DATABASE_NAME))
            .withEnv(Map.of("SPRING_DATASOURCE_USERNAME", DATABASE_NAME))
            .withEnv(Map.of("SPRING_DATASOURCE_PASSWORD", DATABASE_PASSWORD))
            .withEnv(Map.of("SPRING_JPA_HIBERNATE_DDL_AUTO", "update"))
            .withEnv(Map.of("SPRING_LIQUIBASE_URL", "jdbc:postgresql://db:" + PORT_DB + "/" + DATABASE_NAME))
            .withEnv(Map.of("SPRING_LIQUIBASE_USER", DATABASE_NAME))
            .withEnv(Map.of("SPRING_LIQUIBASE_PASSWORD", DATABASE_PASSWORD))
            .withEnv(Map.of("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:29092"))
            .withEnv(Map.of("TZ", "Europe/Moscow"))
            .dependsOn(databaseContainer, zookeeperContainer, kafkaContainer, redisContainer, minioContainer);
    @Container
    private final static GenericContainer<?> consumerContainer = new GenericContainer<>(DockerImageName.parse("sma-consumer_app"))
            .withNetwork(CLOUD_NETWORK)
            .withExposedPorts(PORT_CONSUMER)
            .withEnv(Map.of("SPRING_DATASOURCE_URL", "jdbc:postgresql://db:" + PORT_DB + "/" + DATABASE_NAME))
            .withEnv(Map.of("SPRING_DATASOURCE_USERNAME", DATABASE_NAME))
            .withEnv(Map.of("SPRING_DATASOURCE_PASSWORD", DATABASE_PASSWORD))
            .withEnv(Map.of("SPRING_JPA_HIBERNATE_DDL_AUTO", "update"))
            .withEnv(Map.of("SPRING_LIQUIBASE_URL", "jdbc:postgresql://db:" + PORT_DB + "/" + DATABASE_NAME))
            .withEnv(Map.of("SPRING_LIQUIBASE_USER", DATABASE_NAME))
            .withEnv(Map.of("SPRING_LIQUIBASE_PASSWORD", DATABASE_PASSWORD))
            .withEnv(Map.of("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:29092"))
            .withEnv(Map.of("TZ", "Europe/Moscow"))
            .dependsOn(databaseContainer, zookeeperContainer, kafkaContainer, producerContainer, redisContainer, minioContainer);

    @Test
    void contextDatabaseIsRunning() {
        Assertions.assertTrue(databaseContainer.isRunning());
    }

    @Test
    void contextProducerIsRunning() {
        Assertions.assertTrue(producerContainer.isRunning());
    }

    @Test
    void contextConsumerIsRunning() {
        Assertions.assertTrue(consumerContainer.isRunning());
    }

    @Test
    void contextRedisIsRunning() {
        Assertions.assertTrue(redisContainer.isRunning());
    }

    @Test
    void contextMinioIsRunning() {
        Assertions.assertTrue(minioContainer.isRunning());
    }

    @Test
    void contextZookeeperIsRunning() {
        Assertions.assertTrue(zookeeperContainer.isRunning());
    }

    @Test
    void contextKafkaIsRunning() {
        Assertions.assertTrue(kafkaContainer.isRunning());
    }

}
