package com.utochkin.kafkaproducerforsma.services;

import com.utochkin.kafkaproducerforsma.dto.PostDto;
import com.utochkin.kafkaproducerforsma.sender.KafkaSenderService;
import com.utochkin.kafkaproducerforsma.utils.BaseSpringTestFull;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class KafkaSenderServiceTest extends BaseSpringTestFull {

    @Autowired
    private KafkaSenderService kafkaSenderService;

    @Test
    void send() {
        PostDto message = PostDto.builder().id(123L).build();
        Long userId = 678L;
        kafkaSenderService.send(message, userId);

        ConsumerRecord<Long, String> actual = readOutboundMessage("topic-notification-user");

        Assert.assertEquals(actual.key(), userId);
    }
}
