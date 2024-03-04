package com.utochkin.kafkaproducerforsma.services;

import com.utochkin.kafkaproducerforsma.dto.ChatDto;
import com.utochkin.kafkaproducerforsma.dto.UserDto;
import com.utochkin.kafkaproducerforsma.mappers.ChatMapper;
import com.utochkin.kafkaproducerforsma.mappers.MessageMapper;
import com.utochkin.kafkaproducerforsma.models.Chat;
import com.utochkin.kafkaproducerforsma.models.Role;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.repository.ChatRepository;
import com.utochkin.kafkaproducerforsma.repository.MessageRepository;
import com.utochkin.kafkaproducerforsma.repository.UserRepository;
import com.utochkin.kafkaproducerforsma.services.impl.ChatServiceImpl;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    private User user1;
    private User user2;
    private UserDto toUserDto1;
    private UserDto toUserDto2;
    private Chat chat;
    private ChatDto chatDto;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void getChatById() {
        user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .build();

        user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .build();

        chat = Chat.builder()
                .id(1L)
                .users(Set.of(user1, user2))
                .lastMessage("Msg")
                .build();

        chatDto = new ChatDto(1L, null, "Msg", user1.getId(), user2.getId(), Set.of(user1, user2));

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

        Assertions.assertTrue(chat.getUsers().contains(user1));

        when(chatMapper.toDto(chat)).thenReturn(chatDto);

        when(chatService.getLastMessage(chat.getId())).thenReturn(chat.getLastMessage());
        chatDto.setLastMessage(chat.getLastMessage());

        Assertions.assertEquals(chatDto, chatService.getChatById(chat.getId()));
    }

    @Test
    void createChat() {
//        Set<User> friendForUser1 = new HashSet<>();
//        friendForUser1.add(user2);
//        Set<User> friendForUser2 = new HashSet<>();
//        friendForUser2.add(user1);

        user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .chats(Collections.emptySet())
                .build();

        user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .chats(Collections.emptySet())
                .build();

        user1.addFriend(user2);
        user2.addFriend(user1);

        chatDto = new ChatDto(1L, null, "Msg", user1.getId(), user2.getId(), null);

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userService.getById(chatDto.getFirstUserId())).thenReturn(user1);
        when(userService.getById(chatDto.getSecondUserId())).thenReturn(user2);

        Assertions.assertEquals(user1.getFriends(), Set.of(user2));
        Assertions.assertFalse(user1.getChats().stream().anyMatch(chat -> user2.getChats().contains(chat)));

        chat = new Chat(1L, LocalDateTime.now(), "Msg", Set.of(user1, user2), Collections.emptyList());

        when(chatMapper.toChat(chatDto)).thenReturn(chat);

        chatRepository.save(chat);

        ChatDto newChatDto = new ChatDto(1L, chat.getCreatedAt(), "Msg", user1.getId(), user2.getId(), Set.of(user1, user2));

        when(chatMapper.toDto(chat)).thenReturn(newChatDto);

        Assertions.assertEquals(newChatDto, chatService.createChat(chatDto));
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
    void getAllMessagesInChat() {
    }

    @Test
    void getAllChats() {
    }
}