package com.utochkin.kafkaproducerforsma.services;

import com.utochkin.kafkaproducerforsma.dto.ChatDto;
import com.utochkin.kafkaproducerforsma.dto.MessageDto;
import com.utochkin.kafkaproducerforsma.dto.UpdateMessageDto;
import com.utochkin.kafkaproducerforsma.dto.request.MessageDeleteIdRequest;
import com.utochkin.kafkaproducerforsma.exceptions.BadInputDataException;
import com.utochkin.kafkaproducerforsma.mappers.ChatMapper;
import com.utochkin.kafkaproducerforsma.mappers.MessageMapper;
import com.utochkin.kafkaproducerforsma.models.Chat;
import com.utochkin.kafkaproducerforsma.models.Message;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.repository.ChatRepository;
import com.utochkin.kafkaproducerforsma.repository.MessageRepository;
import com.utochkin.kafkaproducerforsma.services.impl.MessageServiceImpl;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
                .lastMessage("Last msg")
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
        when(messageMapper.toMessage(messageDto, user1, chat)).thenReturn(message);

        when(chatRepository.findById(messageDto.getChatId())).thenReturn(Optional.of(chat));

        when(messageRepository.getLastMessageFromChat(chat.getId())).thenReturn("Last msg");
        chat.setLastMessage("Last msg");

        when(messageMapper.toMessageDto(message)).thenReturn(messageDto);

        Assertions.assertEquals(messageDto, messageService.addMessage(messageDto));
        verify(messageRepository, times(1)).save(message);
        verify(chatRepository, times(1)).save(chat);
    }

    @Test
    void addMessageBadInputDataException() {
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
                .lastMessage("Last msg")
                .users(Set.of(user2))
                .messages(new ArrayList<>())
                .build();

        ChatDto chatDto = new ChatDto(1L, chat.getCreatedAt(), chat.getLastMessage(), null, user2.getId(), Set.of(user2));

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
                .sender(user2)
                .build();

        user1.addFriend(user2);
        user2.addFriend(user1);
        user2.setMessages(List.of(message));
        chat.setMessages(List.of(message));

        when(userService.findByName(messageDto.getSenderName())).thenReturn(user1);
        when(chatService.getChatByIdFromMessageDto(messageDto.getChatId())).thenReturn(chatDto);

        Assertions.assertFalse(chatDto.getUsers().contains(user1));

        Assertions.assertThrows(BadInputDataException.class, () -> messageService.addMessage(messageDto));
        verify(messageRepository, never()).save(message);
        verify(chatRepository, never()).save(chat);
    }

    @Test
    void deleteById() {
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

        MessageDeleteIdRequest messageDeleteIdRequest = MessageDeleteIdRequest.builder()
                .idChat(1L)
                .idMessage(1L)
                .build();

        Chat chat = Chat.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .lastMessage("Last msg")
                .users(Set.of(user1, user2))
                .messages(new ArrayList<>())
                .build();

        MessageDto messageDto = MessageDto.builder()
                .chatId(1L)
                .text("Text")
                .senderName("Sergey")
                .sentAt(LocalDateTime.now())
                .build();

        user1.addFriend(user2);
        user2.addFriend(user1);

        when(chatRepository.findById(messageDto.getChatId())).thenReturn(Optional.of(chat));

        when(messageRepository.getLastMessageFromChat(chat.getId())).thenReturn("Last msg");
        chat.setLastMessage("Last msg");

        Assertions.assertEquals(new MessageDeleteIdRequest(messageDeleteIdRequest.getIdMessage(), messageDeleteIdRequest.getIdChat(), true), messageService.deleteById(messageDeleteIdRequest));
        verify(messageRepository, times(1)).deleteById(messageDeleteIdRequest.getIdMessage());
        verify(chatRepository, times(1)).save(chat);
    }

    @Test
    void update() {
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
                .lastMessage("Last msg")
                .users(Set.of(user1, user2))
                .messages(new ArrayList<>())
                .build();

        ChatDto chatDto = new ChatDto(1L, chat.getCreatedAt(), chat.getLastMessage(), user1.getId(), user2.getId(), Set.of(user1, user2));

        Message message = Message.builder()
                .id(1L)
                .chat(chat)
                .sentAt(LocalDateTime.now())
                .text("Text")
                .sender(user1)
                .build();

        UpdateMessageDto updateMessageDto = new UpdateMessageDto(1L, chat.getId(), "Updated text", user1.getName(), false);

        Message updateMessage = Message.builder()
                .id(1L)
                .chat(chat)
                .sentAt(LocalDateTime.now())
                .text(updateMessageDto.getUpdatedText())
                .sender(user1)
                .build();

        user1.addFriend(user2);
        user2.addFriend(user1);
        user1.setMessages(List.of(message));
        chat.setMessages(List.of(message));

        when(userService.findByName(updateMessageDto.getSenderName())).thenReturn(user1);
        when(chatService.getChatByIdFromMessageDto(updateMessageDto.getChatId())).thenReturn(chatDto);

        Assertions.assertTrue(chatDto.getUsers().contains(user1));

        when(messageRepository.findById(updateMessageDto.getId())).thenReturn(Optional.of(message));

        when(messageMapper.update(message, updateMessageDto)).thenReturn(updateMessage);

        when(chatRepository.findById(updateMessageDto.getChatId())).thenReturn(Optional.of(chat));

        when(messageRepository.getLastMessageFromChat(chat.getId())).thenReturn("Last msg");
        chat.setLastMessage("Last msg");

        when(messageMapper.toUpdateMessageDto(updateMessage)).thenReturn(updateMessageDto);
        updateMessageDto.setUpdated(true);
        updateMessageDto.setSenderName(updateMessageDto.getSenderName());

        Assertions.assertEquals(new UpdateMessageDto(1L, chat.getId(), "Updated text", user1.getName(), true), messageService.update(updateMessageDto));
        verify(messageRepository, times(1)).save(updateMessage);
    }

    @Test
    void updateBadInputDataException() {
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
                .lastMessage("Last msg")
                .users(Set.of(user2))
                .messages(new ArrayList<>())
                .build();

        ChatDto chatDto = new ChatDto(1L, chat.getCreatedAt(), chat.getLastMessage(), null, user2.getId(), Set.of(user2));

        Message message = Message.builder()
                .id(1L)
                .chat(chat)
                .sentAt(LocalDateTime.now())
                .text("Text")
                .sender(user2)
                .build();

        UpdateMessageDto updateMessageDto = new UpdateMessageDto(1L, chat.getId(), "Updated text", user2.getName(), false);

        Message updateMessage = Message.builder()
                .id(1L)
                .chat(chat)
                .sentAt(LocalDateTime.now())
                .text(updateMessageDto.getUpdatedText())
                .sender(user2)
                .build();

        user1.addFriend(user2);
        user2.addFriend(user1);
        user2.setMessages(List.of(message));
        chat.setMessages(List.of(message));

        when(userService.findByName(updateMessageDto.getSenderName())).thenReturn(user1);
        when(chatService.getChatByIdFromMessageDto(updateMessageDto.getChatId())).thenReturn(chatDto);

        Assertions.assertFalse(chatDto.getUsers().contains(user1));

        Assertions.assertThrows(BadInputDataException.class, () -> messageService.update(updateMessageDto));
        verify(messageRepository, never()).save(updateMessage);
    }

}