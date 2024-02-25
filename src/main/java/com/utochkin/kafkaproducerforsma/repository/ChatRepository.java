package com.utochkin.kafkaproducerforsma.repository;


import com.utochkin.kafkaproducerforsma.models.Chat;
import com.utochkin.kafkaproducerforsma.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

}
