package com.utochkin.kafkaproducerforsma.services;

import com.utochkin.kafkaproducerforsma.dto.PostDto;
import com.utochkin.kafkaproducerforsma.sender.KafkaSenderService;
import com.utochkin.kafkaproducerforsma.utils.BaseSpringTestFull;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaSenderServiceTest extends BaseSpringTestFull {

    @Autowired
    private KafkaSenderService kafkaSenderService;

    @Test
    void send() {
        PostDto postDto = PostDto.builder()
                .id(123L)
                .description("New description")
                .message("New message")
                .build();
        Long userId = 678L;
        kafkaSenderService.send(postDto, userId);

        ConsumerRecord<Long, String> actual = readOutboundMessage("topic-notification-user");

        Assertions.assertEquals(actual.key(), userId);
        Assertions.assertEquals(actual.value(), asJsonString(postDto));
    }

    public static String asJsonString(final Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
