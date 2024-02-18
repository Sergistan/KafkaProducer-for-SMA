package com.utochkin.kafkaproducerforsma.services.interfaces;


import com.utochkin.kafkaproducerforsma.dto.ChatDto;

public interface ChatService {
    void leaveChat(Long userId , Long chatId);

    void joinChat(Long userId, Long chatId);

    ChatDto createChat(ChatDto chatDto);

    ChatDto getChatById(Long chatId);

    void deleteChatById(Long chatId);

    String getLastMessage (Long chatId);
}
