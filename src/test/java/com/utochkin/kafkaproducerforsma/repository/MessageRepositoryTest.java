package com.utochkin.kafkaproducerforsma.repository;

import com.utochkin.kafkaproducerforsma.models.Chat;
import com.utochkin.kafkaproducerforsma.models.Message;
import com.utochkin.kafkaproducerforsma.models.Role;
import com.utochkin.kafkaproducerforsma.models.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRepository chatRepository;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private User user1;
    private User user2;
    private Chat chatWithLastMessage;
    private Chat chatWithoutLastMessage;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .messages(new ArrayList<>())
                .chats(new HashSet<>())
                .build();

        user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .messages(new ArrayList<>())
                .chats(new HashSet<>())
                .build();

        user1.addFriend(user2);
        user2.addFriend(user1);

        chatWithLastMessage = Chat.builder()
                .id(1L)
                .createdAt(LocalDateTime.parse("2024-03-20T16:58:22.014357700"))
                .lastMessage("Text 2")
                .users(new HashSet<>())
                .messages(new ArrayList<>())
                .build();

        chatWithoutLastMessage = Chat.builder()
                .id(1L)
                .createdAt(LocalDateTime.parse("2024-03-20T16:58:22.014357700"))
                .lastMessage("")
                .users(new HashSet<>())
                .messages(null)
                .build();
    }

    @Test
    void getLastMessageFromChat() {
        chatWithLastMessage.setUsers(Set.of(user1, user2));

        Message message1 = Message.builder()
                .id(1L)
                .text("Text 1")
                .sentAt(LocalDateTime.parse("2024-03-20T16:59:22.014357700"))
                .sender(user1)
                .chat(chatWithLastMessage)
                .build();

        Message message2 = Message.builder()
                .id(2L)
                .text("Text 2")
                .sentAt(LocalDateTime.parse("2024-03-20T17:00:22.014357700"))
                .sender(user2)
                .chat(chatWithLastMessage)
                .build();

        chatWithLastMessage.setMessages(List.of(message1, message2));

        userRepository.save(user1);
        userRepository.save(user2);
        chatRepository.save(chatWithLastMessage);
        messageRepository.save(message1);
        messageRepository.save(message2);

        Assertions.assertEquals(message2.getText(), messageRepository.getLastMessageFromChat(chatWithLastMessage.getId()));
    }

    @Test
    void getLastMessageFromChatWhenNotLastMessage() {
        chatWithoutLastMessage.setUsers(Set.of(user1, user2));

        userRepository.save(user1);
        userRepository.save(user2);
        chatRepository.save(chatWithoutLastMessage);

        Assertions.assertNull(messageRepository.getLastMessageFromChat(chatWithoutLastMessage.getId()));
    }

    @Test
    void getAllMessagesInChat() {
        chatWithLastMessage.setUsers(Set.of(user1, user2));

        Message message1 = Message.builder()
                .id(1L)
                .text("Text 1")
                .sentAt(LocalDateTime.parse("2024-03-20T16:59:22.014357700"))
                .sender(user1)
                .chat(chatWithLastMessage)
                .build();

        Message message2 = Message.builder()
                .id(2L)
                .text("Text 2")
                .sentAt(LocalDateTime.parse("2024-03-20T17:00:22.014357700"))
                .sender(user2)
                .chat(chatWithLastMessage)
                .build();

        chatWithLastMessage.setMessages(List.of(message1, message2));

        userRepository.save(user1);
        userRepository.save(user2);
        chatRepository.save(chatWithLastMessage);
        Message messageSave1 = messageRepository.save(message1);
        Message messageSave2 = messageRepository.save(message2);

        List<Message> allMessagesInChat = messageRepository.getAllMessagesInChat(chatWithLastMessage.getId());

        Assertions.assertEquals(allMessagesInChat.get(0), messageSave1);
        Assertions.assertEquals(allMessagesInChat.get(1), messageSave2);
    }

    @Test
    void getAllMessagesInChatWhenNotExistMessages() {
        chatWithoutLastMessage.setUsers(Set.of(user1, user2));

        userRepository.save(user1);
        userRepository.save(user2);
        chatRepository.save(chatWithoutLastMessage);

        Assertions.assertEquals(Collections.emptyList(), messageRepository.getAllMessagesInChat(chatWithoutLastMessage.getId()));
    }
}