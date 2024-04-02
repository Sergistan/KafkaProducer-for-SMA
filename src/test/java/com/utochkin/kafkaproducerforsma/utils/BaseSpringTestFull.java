package com.utochkin.kafkaproducerforsma.utils;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Testcontainers
@ActiveProfiles("it")
public class BaseSpringTestFull {
    static String DOCKER_IMAGE_KAFKA = "confluentinc/cp-kafka:7.4.0";
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse(DOCKER_IMAGE_KAFKA));
    static KafkaTemplate<Long, Object> kafkaTemplate;
    static Consumer<Long, String> kafkaConsumer;

    protected ConsumerRecord<Long, String> readOutboundMessage(String outboundTopic) {
        kafkaConsumer.subscribe(List.of(outboundTopic));
        return KafkaTestUtils.getSingleRecord(kafkaConsumer, outboundTopic, Duration.ofSeconds(5000));
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        kafkaContainer.start();
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);

        kafkaTemplate = prepareKafkaTemplate();
        kafkaConsumer = prepareConsumer();
    }

    static private KafkaTemplate<Long, Object> prepareKafkaTemplate() {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        producerProps.put(ProducerConfig.RETRIES_CONFIG, 5);

        DefaultKafkaProducerFactory<Long, Object> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        return new KafkaTemplate<>(producerFactory);
    }

    static private Consumer<Long, String> prepareConsumer() {
        Map<String, Object>  consumerProps = KafkaTestUtils.consumerProps(kafkaContainer.getBootstrapServers(), "integration-test-consumer", "true");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(consumerProps, new LongDeserializer(), new StringDeserializer()).createConsumer();
    }
}
