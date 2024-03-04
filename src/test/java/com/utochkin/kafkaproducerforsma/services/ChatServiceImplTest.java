package com.utochkin.kafkaproducerforsma.services;

import com.utochkin.kafkaproducerforsma.mappers.ChatMapper;
import com.utochkin.kafkaproducerforsma.mappers.MessageMapper;
import com.utochkin.kafkaproducerforsma.repository.ChatRepository;
import com.utochkin.kafkaproducerforsma.repository.MessageRepository;
import com.utochkin.kafkaproducerforsma.repository.UserRepository;
import com.utochkin.kafkaproducerforsma.services.impl.ChatServiceImpl;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @InjectMocks
    private ChatServiceImpl chatService;
    @Mock
    private ChatRepository chatRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ChatMapper chatMapper;
    @Mock
    private MessageMapper messageMapper;
    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;

    @Test
    void getChatById() {

    }

    @Test
    void getChatByIdFromMessageDto() {
    }

    @Test
    void createChat() {
    }

    @Test
    void deleteChatById() {
    }

    @Test
    void joinChat() {
    }

    @Test
    void leaveChat() {
    }

    @Test
    void getLastMessage() {
    }

    @Test
    void getLastMessageFromMessageDto() {
    }

    @Test
    void getAllMessagesInChat() {
    }

    @Test
    void getAllChats() {
    }
}