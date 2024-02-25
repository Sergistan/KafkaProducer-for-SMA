package com.utochkin.kafkaproducerforsma.services.interfaces;


import com.utochkin.kafkaproducerforsma.dto.ChatDto;
import com.utochkin.kafkaproducerforsma.dto.MessageDto;

import java.util.List;

public interface ChatService {
    Long leaveChat(Long chatId);

    Long joinChat(Long chatId);

    ChatDto createChat(ChatDto chatDto);

    ChatDto getChatById(Long chatId);

    void deleteChatById(Long chatId);

    String getLastMessage (Long chatId);

    List<ChatDto> getAllChats();

    List<MessageDto> getAllMessagesInChat(Long chatId);

    ChatDto getChatByIdFromMessageDto(Long chatId);
    String getLastMessageFromMessageDto(Long chatId);
}
