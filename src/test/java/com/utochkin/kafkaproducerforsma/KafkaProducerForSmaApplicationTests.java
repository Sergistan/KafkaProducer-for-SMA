//package com.utochkin.kafkaproducerforsma;
//
//import org.jetbrains.annotations.NotNull;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.*;
//import org.testcontainers.containers.output.Slf4jLogConsumer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.utility.DockerImageName;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Testcontainers
//class KafkaProducerForSmaApplicationTests {
//    private static final int PORT_DB = 5432;
//    private static final int PORT_PRODUCER = 8080;
//    private static final int PORT_CONSUMER = 8081;
//    private static final int PORT_REDIS = 6379;
//    private static final String DATABASE_NAME = "SMA";
//    private static final String DATABASE_USERNAME = "postgres";
//    private static final String DATABASE_PASSWORD = "1234";
//    private static final Network CLOUD_NETWORK = Network.newNetwork();
//    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerForSmaApplicationTests.class);
//
//    @DynamicPropertySource
//    static void dynamicProperties(@NotNull DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", databaseContainer::getJdbcUrl);
//        registry.add("spring.datasource.username", databaseContainer::getUsername);
//        registry.add("spring.datasource.password", databaseContainer::getPassword);
//        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
////        registry.add("spring.data.redis.host", redisContainer::getHost);
////        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
//        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.yaml");
//        registry.add("spring.liquibase.enabled", () -> true);
////        registry.add("minio.url", minioContainer::getS3URL);
////        registry.add("minio.accessKey", minioContainer::getUserName);
////        registry.add("minio.secretKey", minioContainer::getPassword);
//    }
//
//    @Container
//    private final static PostgreSQLContainer<?> databaseContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres"))
//            .withNetwork(CLOUD_NETWORK)
//            .withExposedPorts(PORT_DB)
//            .withDatabaseName(DATABASE_NAME)
//            .withUsername(DATABASE_USERNAME)
//            .withPassword(DATABASE_PASSWORD)
//            .withEnv("TZ", "Europe/Moscow")
//            .withLogConsumer(new Slf4jLogConsumer(LOGGER));
//
//    @Container
//    private final static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis"))
//            .withNetwork(CLOUD_NETWORK)
//            .withExposedPorts(PORT_REDIS)
//            .withCommand("redis-server --save 20 1 --loglevel warning --requirepass MjEzNDU2N3F3ZXJ0eWRmZ2RmZ2hzZGE")
//            .withEnv("TZ", "Europe/Moscow")
//            .withLogConsumer(new Slf4jLogConsumer(LOGGER));
//
//    @Container
//    private final static MinIOContainer minioContainer = new MinIOContainer(DockerImageName.parse("minio/minio"))
//            .withNetwork(CLOUD_NETWORK)
//            .withEnv("TZ", "Europe/Moscow")
//            .withLogConsumer(new Slf4jLogConsumer(LOGGER));
////            .withCommand("minio server /data/minio --console-address ':9000'");
//
//    @Container
//    private final static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))
//            .withNetwork(CLOUD_NETWORK)
//            .withListener(() -> "kafka:29092")
//            .withEmbeddedZookeeper()
//            .withEnv("TZ", "Europe/Moscow")
//            .withLogConsumer(new Slf4jLogConsumer(LOGGER));
//    @Container
//    private final static GenericContainer<?> producerContainer = new GenericContainer<>(DockerImageName.parse("sma-producer_app"))
//            .withNetwork(CLOUD_NETWORK)
//            .withExposedPorts(PORT_PRODUCER)
//            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://db:" + PORT_DB + "/" + DATABASE_NAME)
//            .withEnv("SPRING_DATASOURCE_USERNAME", DATABASE_NAME)
//            .withEnv("SPRING_DATASOURCE_PASSWORD", DATABASE_PASSWORD)
//            .withEnv("SPRING_LIQUIBASE_URL", "jdbc:postgresql://db:" + PORT_DB + "/" + DATABASE_NAME)
//            .withEnv("SPRING_LIQUIBASE_USER", DATABASE_NAME)
//            .withEnv("SPRING_LIQUIBASE_PASSWORD", DATABASE_PASSWORD)
//            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:29092")
//            .withEnv("TZ", "Europe/Moscow")
//            .dependsOn(databaseContainer, kafkaContainer, redisContainer, minioContainer)
//            .withLogConsumer(new Slf4jLogConsumer(LOGGER));
//    @Container
//    private final static GenericContainer<?> consumerContainer = new GenericContainer<>(DockerImageName.parse("sma-consumer_app"))
//            .withNetwork(CLOUD_NETWORK)
//            .withExposedPorts(PORT_CONSUMER)
//            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://db:" + PORT_DB + "/" + DATABASE_NAME)
//            .withEnv("SPRING_DATASOURCE_USERNAME", DATABASE_NAME)
//            .withEnv("SPRING_DATASOURCE_PASSWORD", DATABASE_PASSWORD)
//            .withEnv("SPRING_LIQUIBASE_URL", "jdbc:postgresql://db:" + PORT_DB + "/" + DATABASE_NAME)
//            .withEnv("SPRING_LIQUIBASE_USER", DATABASE_NAME)
//            .withEnv("SPRING_LIQUIBASE_PASSWORD", DATABASE_PASSWORD)
//            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:29092")
//            .withEnv("TZ", "Europe/Moscow")
//            .dependsOn(databaseContainer, kafkaContainer, producerContainer, redisContainer, minioContainer)
//            .withLogConsumer(new Slf4jLogConsumer(LOGGER));
//
//    @Test
//    void contextDatabaseIsRunning() {
//        Assertions.assertTrue(databaseContainer.isRunning());
//    }
//
//    @Test
//    void contextProducerIsRunning() {
//        System.out.println("");
//        Assertions.assertTrue(producerContainer.isRunning());
//    }
//
//    @Test
//    void contextConsumerIsRunning() {
//        Assertions.assertTrue(consumerContainer.isRunning());
//    }
//
//    @Test
//    void contextRedisIsRunning() {
//        Assertions.assertTrue(redisContainer.isRunning());
//    }
//
//    @Test
//    void contextMinioIsRunning() {
//        Assertions.assertTrue(minioContainer.isRunning());
//    }
//
//    @Test
//    void contextKafkaIsRunning() {
//        Assertions.assertTrue(kafkaContainer.isRunning());
//    }
//
//}
