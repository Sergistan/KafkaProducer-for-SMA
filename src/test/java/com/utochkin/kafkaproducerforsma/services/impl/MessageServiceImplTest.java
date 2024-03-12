package com.utochkin.kafkaproducerforsma.services.impl;

import com.utochkin.kafkaproducerforsma.dto.ChatDto;
import com.utochkin.kafkaproducerforsma.dto.MessageDto;
import com.utochkin.kafkaproducerforsma.mappers.ChatMapper;
import com.utochkin.kafkaproducerforsma.mappers.MessageMapper;
import com.utochkin.kafkaproducerforsma.models.Chat;
import com.utochkin.kafkaproducerforsma.models.Message;
import com.utochkin.kafkaproducerforsma.models.Role;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.repository.ChatRepository;
import com.utochkin.kafkaproducerforsma.repository.MessageRepository;
import com.utochkin.kafkaproducerforsma.services.interfaces.ChatService;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MessageServiceImplTest {

    @InjectMocks
    private MessageServiceImpl messageService;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserService userService;
    @Mock
    private ChatService chatService;
    @Mock
    private MessageMapper messageMapper;
    @Mock
    private ChatMapper chatMapper;
    @Mock
    private ChatRepository chatRepository;

    @Test
    void addMessage() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .messages(new ArrayList<>())
                .chats(new HashSet<>())
                .friends(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .messages(new ArrayList<>())
                .chats(new HashSet<>())
                .friends(new HashSet<>())
                .build();

        Chat chat = Chat.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .lastMessage("Msg")
                .users(Set.of(user1, user2))
                .messages(new ArrayList<>())
                .build();

        ChatDto chatDto = new ChatDto(1L, chat.getCreatedAt(), chat.getLastMessage(), user1.getId(), user2.getId(), Set.of(user1, user2));

        MessageDto messageDto = MessageDto.builder()
                .chatId(1L)
                .text("Text")
                .senderName("Sergey")
                .sentAt(LocalDateTime.now())
                .build();

        Message message = Message.builder()
                .id(1L)
                .chat(chat)
                .sentAt(messageDto.getSentAt())
                .text(messageDto.getText())
                .sender(user1)
                .build();

        user1.addFriend(user2);
        user2.addFriend(user1);
        user1.setMessages(List.of(message));
        chat.setMessages(List.of(message));

        when(userService.findByName(messageDto.getSenderName())).thenReturn(user1);
        when(chatService.getChatByIdFromMessageDto(messageDto.getChatId())).thenReturn(chatDto);

        Assertions.assertTrue(chatDto.getUsers().contains(user1));

        when(chatMapper.toChat(chatDto)).thenReturn(chat);
        when(messageMapper.toMessage(messageDto,user1,chat)).thenReturn(message);
    }

    @Test
    void deleteById() {
    }

    @Test
    void update() {
    }
}