package com.utochkin.kafkaproducerforsma.services;

import com.utochkin.kafkaproducerforsma.dto.UserDto;
import com.utochkin.kafkaproducerforsma.exceptions.AccessDeniedException;
import com.utochkin.kafkaproducerforsma.exceptions.BadInputDataException;
import com.utochkin.kafkaproducerforsma.exceptions.UserNotFoundException;
import com.utochkin.kafkaproducerforsma.mappers.UserMapper;
import com.utochkin.kafkaproducerforsma.models.Chat;
import com.utochkin.kafkaproducerforsma.models.Role;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.repository.UserRepository;
import com.utochkin.kafkaproducerforsma.services.impl.ChatServiceImpl;
import com.utochkin.kafkaproducerforsma.services.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private ChatServiceImpl chatService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void findByName() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .build();

        doReturn(Optional.of(user1)).when(userRepository).findByName(user1.getName());
        Assertions.assertEquals(user1, userService.findByName(user1.getName()));
    }

    @Test
    void notFindByName() {
        doThrow(UserNotFoundException.class).when(userRepository).findByName(Mockito.anyString());
        Assertions.assertThrows(UserNotFoundException.class, () -> userService.findByName(Mockito.anyString()));
    }

    @Test
    void getById() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .build();

        Mockito.doReturn(Optional.of(user1)).when(userRepository).findById(user1.getId());
        Assertions.assertEquals(user1, userService.getById(user1.getId()));
    }

    @Test
    void notFindById() {
        doThrow(UserNotFoundException.class).when(userRepository).findById(Mockito.anyLong());
        Assertions.assertThrows(UserNotFoundException.class, () -> userService.getById(Mockito.anyLong()));
    }

    @Test
    void createUser() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Sergey")
                .password("111")
                .email("sergistan.utochkin@yandex.ru")
                .build();

        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .email("sergistan.utochkin@yandex.ru")
                .password(passwordEncoder.encode("111"))
                .role(Role.ROLE_USER)
                .build();

        doReturn(user).when(userMapper).toEntity(userDto);
        doReturn("qwerty123").when(passwordEncoder).encode(userDto.getPassword());
        user.setPassword("qwerty123");
        Assertions.assertEquals(userRepository.save(user), userService.createUser(userDto));
    }

    @Test
    void createFriendRequest() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        doReturn(Optional.of(user1)).when(userRepository).findByName(user1.getName());

        doReturn(Optional.of(user2)).when(userRepository).findById(user2.getId());

        Assertions.assertFalse(user1.getFriends().contains(user2));

        user2.addFollower(user1);
        user2.addFriendRequest(user1);

        Assertions.assertTrue(user2.getFollowers().contains(user1));
        Assertions.assertTrue(user2.getFriendRequests().contains(user1));

        Assertions.assertEquals(user1.getId(), userService.createFriendRequest(user2.getId()));
    }

    @Test
    void createFriendRequestYourself() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        doReturn(Optional.of(user1)).when(userRepository).findByName(user1.getName());

        Assertions.assertThrows(BadInputDataException.class, () -> userService.createFriendRequest(user1.getId()));
    }

    @Test
    void createFriendRequestWhenAlreadyFriend() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        user1.addFriend(user2);
        user2.addFriend(user1);

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        doReturn(Optional.of(user1)).when(userRepository).findByName(user1.getName());

        doReturn(Optional.of(user2)).when(userRepository).findById(user2.getId());

        Assertions.assertThrows(BadInputDataException.class, () -> userService.createFriendRequest(user2.getId()));
    }

    @Test
    void acceptFriendRequest() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        user2.addFollower(user1);
        user2.addFriendRequest(user1);

        doReturn(user2.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user2.getName())).thenReturn(Optional.of(user2));
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));

        Assertions.assertTrue(user2.getFriendRequests().contains(user1));

        Assertions.assertFalse(user1.getFriends().contains(user2));

        Assertions.assertEquals(user2.getId(), userService.acceptFriendRequest(user1.getId()));
        Assertions.assertTrue(user1.getFriends().contains(user2));
        Assertions.assertTrue(user2.getFriends().contains(user1));
        Assertions.assertTrue(user1.getFollowers().contains(user2));
    }

    @Test
    void acceptFriendRequestYourself() {
        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        doReturn(user2.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user2.getName())).thenReturn(Optional.of(user2));
        Assertions.assertThrows(BadInputDataException.class, () -> userService.createFriendRequest(user2.getId()));
    }

    @Test
    void acceptFriendRequestAlreadyFriend() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        user1.addFriend(user2);
        user2.addFriend(user1);

        doReturn(user2.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user2.getName())).thenReturn(Optional.of(user2));
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));

        Assertions.assertThrows(BadInputDataException.class, () -> userService.createFriendRequest(user1.getId()));
    }

    @Test
    void refuseFriendRequest() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        user2.addFollower(user1);
        user2.addFriendRequest(user1);

        doReturn(user2.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user2.getName())).thenReturn(Optional.of(user2));
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));

        Assertions.assertTrue(user2.getFriendRequests().contains(user1));

        Assertions.assertFalse(user1.getFriends().contains(user2));

        Assertions.assertEquals(user2.getId(), userService.refuseFriendRequest(user1.getId()));
        Assertions.assertFalse(user1.getFriends().contains(user2));
        Assertions.assertFalse(user2.getFriends().contains(user1));
        Assertions.assertFalse(user1.getFollowers().contains(user2));
        Assertions.assertTrue(user2.getFollowers().contains(user1));
    }

    @Test
    void refuseFriendRequestYourself() {
        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        doReturn(user2.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user2.getName())).thenReturn(Optional.of(user2));
        Assertions.assertThrows(BadInputDataException.class, () -> userService.refuseFriendRequest(user2.getId()));
    }

    @Test
    void refuseFriendRequestAlreadyFriend() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        user1.addFriend(user2);
        user2.addFriend(user1);

        doReturn(user2.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user2.getName())).thenReturn(Optional.of(user2));
        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));

        Assertions.assertThrows(BadInputDataException.class, () -> userService.refuseFriendRequest(user1.getId()));
    }

    @Test
    void refuseFollower() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        user1.addFriend(user2);
        user2.addFriend(user1);
        user1.addFollower(user2);
        user2.addFollower(user1);

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

        Assertions.assertTrue(user1.getFollowers().contains(user2));

        Assertions.assertEquals(user1.getId(), userService.refuseFollower(user2.getId()));
        Assertions.assertTrue(user1.getFriends().contains(user2));
        Assertions.assertTrue(user2.getFriends().contains(user1));
        Assertions.assertTrue(user1.getFollowers().contains(user2));
        Assertions.assertFalse(user2.getFollowers().contains(user1));
    }

    @Test
    void refuseFollowerYourself() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));
        Assertions.assertThrows(BadInputDataException.class, () -> userService.refuseFollower(user1.getId()));
    }

    @Test
    void refuseFollowerDonNotHaveFollower() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        user1.addFollower(user2);
        user2.addFriend(user1);

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

        Assertions.assertThrows(BadInputDataException.class, () -> userService.refuseFollower(user2.getId()));
    }

    @Test
    void deleteFriendWithChat() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        Chat chat = Chat.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .lastMessage("Msg")
                .users(new HashSet<>())
                .messages(Collections.emptyList())
                .build();

        user1.addFriend(user2);
        user2.addFriend(user1);
        user1.addFollower(user2);
        user2.addFollower(user1);
        user1.setChats(Set.of(chat));
        user2.setChats(Set.of(chat));
        chat.setUsers(Set.of(user1, user2));

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

        Assertions.assertTrue(user1.getFriends().contains(user2));

        Assertions.assertTrue(user1.getChats().stream().anyMatch(x -> user2.getChats().contains(x)));

        chatService.deleteChatById(chat.getId());

        Assertions.assertEquals(user1.getId(), userService.deleteFriend(user2.getId()));
        Assertions.assertFalse(user2.getFollowers().contains(user1));
        Assertions.assertFalse(user1.getFriends().contains(user2));
        Assertions.assertFalse(user2.getFriends().contains(user1));
        Assertions.assertTrue(user1.getFollowers().contains(user2));
    }

    @Test
    void deleteFriendWithoutChat() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .chats(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .chats(new HashSet<>())
                .build();

        user1.addFriend(user2);
        user2.addFriend(user1);
        user1.addFollower(user2);
        user2.addFollower(user1);

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

        Assertions.assertTrue(user1.getFriends().contains(user2));

        Assertions.assertTrue(user1.getChats().isEmpty());
        Assertions.assertTrue(user2.getChats().isEmpty());

        Assertions.assertEquals(user1.getId(), userService.deleteFriend(user2.getId()));
        verify(chatService, never()).deleteChatById(anyLong());
        Assertions.assertFalse(user2.getFollowers().contains(user1));
        Assertions.assertFalse(user1.getFriends().contains(user2));
        Assertions.assertFalse(user2.getFriends().contains(user1));
        Assertions.assertTrue(user1.getFollowers().contains(user2));
    }

    @Test
    void deleteFriendDonNotHaveFriend() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

        Assertions.assertThrows(BadInputDataException.class, () -> userService.refuseFollower(user2.getId()));
    }

    @Test
    void deleteFriendYourself() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));
        Assertions.assertThrows(BadInputDataException.class, () -> userService.deleteFriend(user1.getId()));
    }

    @Test
    void getAllUsers() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .build();

        UserDto userDto1 = UserDto.builder()
                .id(1L)
                .name("Sergey")
                .email("sergistan.utochkin@yandex.ru")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Tom")
                .role(Role.ROLE_ADMIN)
                .build();

        doReturn(user2.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.getAllNameAdmins()).thenReturn(List.of(user2.getName()));
        when(userRepository.findAll()).thenReturn(List.of(user1));
        when(userMapper.toListDto(List.of(user1))).thenReturn(List.of(userDto1));

        Assertions.assertEquals(List.of(userDto1), userService.getAllUsers());
    }

    @Test
    void getAllUsersAccessDenied() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Tom")
                .role(Role.ROLE_ADMIN)
                .build();

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.getAllNameAdmins()).thenReturn(List.of(user2.getName()));

        Assertions.assertThrows(AccessDeniedException.class, () -> userService.getAllUsers());
    }

    @Test
    void getAllUsersWhenNoUsers() {
        User user2 = User.builder()
                .id(2L)
                .name("Tom")
                .role(Role.ROLE_ADMIN)
                .build();

        doReturn(user2.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.getAllNameAdmins()).thenReturn(List.of(user2.getName()));
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(userMapper.toListDto(Collections.emptyList())).thenReturn(Collections.emptyList());

        Assertions.assertEquals(Collections.emptyList(), userService.getAllUsers());
    }

    @Test
    void getIdUser() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));
        Assertions.assertEquals(user1.getId(), userService.getIdUser(user1.getName()));
    }

    @Test
    void notGetIdUser() {
        doThrow(UserNotFoundException.class).when(userRepository).findByName(Mockito.anyString());
        Assertions.assertThrows(UserNotFoundException.class, () -> userService.getIdUser(Mockito.anyString()));
    }

    @Test
    void getAllUsersFriends() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        UserDto userDto2 = UserDto.builder()
                .id(2L)
                .name("Ilya")
                .email("dzaga73i98@gmail.com")
                .build();

        User user3 = User.builder()
                .id(3L)
                .name("Alina")
                .password(passwordEncoder.encode("333"))
                .email("umamimi@ya.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        UserDto userDto3 = UserDto.builder()
                .id(3L)
                .name("Alina")
                .email("umamimi@ya.com")
                .build();

        user1.addFriend(user2);
        user1.addFriend(user3);

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));

        Assertions.assertFalse(user1.getFriends().isEmpty());

        when(userMapper.toListDto(user1.getFriends().stream().toList())).thenReturn(List.of(userDto2, userDto3));

        Assertions.assertEquals(List.of(userDto2, userDto3), userService.getAllUsersFriends());
    }

    @Test
    void notGetAllUsersFriends() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));

        Assertions.assertEquals(Collections.emptyList(), userService.getAllUsersFriends());
    }

    @Test
    void getAllUsersFollowers() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        UserDto userDto2 = UserDto.builder()
                .id(2L)
                .name("Ilya")
                .email("dzaga73i98@gmail.com")
                .build();

        User user3 = User.builder()
                .id(3L)
                .name("Alina")
                .password(passwordEncoder.encode("333"))
                .email("umamimi@ya.com")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        UserDto userDto3 = UserDto.builder()
                .id(3L)
                .name("Alina")
                .email("umamimi@ya.com")
                .build();

        user1.addFollower(user2);
        user1.addFollower(user3);

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));

        Assertions.assertFalse(user1.getFollowers().isEmpty());

        when(userMapper.toListDto(user1.getFollowers().stream().toList())).thenReturn(List.of(userDto2, userDto3));

        Assertions.assertEquals(List.of(userDto2, userDto3), userService.getAllUsersFollowers());
    }

    @Test
    void notGetAllUsersFollowers() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(new HashSet<>())
                .followers(new HashSet<>())
                .friendRequests(new HashSet<>())
                .build();

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));

        Assertions.assertEquals(Collections.emptyList(), userService.getAllUsersFollowers());
    }
}