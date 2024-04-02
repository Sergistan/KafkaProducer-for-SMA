package com.utochkin.kafkaproducerforsma.sender;

import com.utochkin.kafkaproducerforsma.dto.PostDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaSenderService {
    private final KafkaTemplate<Long, Object> kafkaTemplate;

    public void send(PostDto post, Long userId) {
        kafkaTemplate.send("topic-notification-user", userId, post);
    }
}
