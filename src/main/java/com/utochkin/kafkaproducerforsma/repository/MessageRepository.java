package com.utochkin.kafkaproducerforsma.repository;


import com.utochkin.kafkaproducerforsma.models.Message;
import com.utochkin.kafkaproducerforsma.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query(value = "SELECT f.text from messages f WHERE f.chat_id = ?1 ORDER BY f.id DESC limit 1", nativeQuery = true)
    String getLastMessageFromChat(Long chatId);

    @Query(value = "SELECT * from messages f WHERE f.chat_id = ?1 ORDER BY f.id", nativeQuery = true)
    List<Message> getAllMessagesInChat(Long chatId);
}
