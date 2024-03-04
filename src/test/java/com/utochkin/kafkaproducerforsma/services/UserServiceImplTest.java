package com.utochkin.kafkaproducerforsma.services;

import com.utochkin.kafkaproducerforsma.dto.UserDto;
import com.utochkin.kafkaproducerforsma.exceptions.UserNotFoundException;
import com.utochkin.kafkaproducerforsma.mappers.UserMapper;
import com.utochkin.kafkaproducerforsma.models.Chat;
import com.utochkin.kafkaproducerforsma.models.Role;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.repository.UserRepository;
import com.utochkin.kafkaproducerforsma.services.impl.ChatServiceImpl;
import com.utochkin.kafkaproducerforsma.services.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private ChatServiceImpl chatService;

    private User user1;
    private User user2;
    private UserDto userDto1;
    private UserDto toUserDto1;
    private UserDto toUserDto2;


    @BeforeEach
    void setUp() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

        toUserDto1 = UserDto.builder()
                .id(1L)
                .name("Sergey")
                .email("sergistan.utochkin@yandex.ru")
                .build();

        toUserDto2 = UserDto.builder()
                .id(2L)
                .name("Ilya")
                .email("dzaga73i98@gmail.com")
                .build();

        userDto1 = UserDto.builder()
                .name("Sergey")
                .password("111")
                .build();
    }


    @Test
    void findByName() {
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
        doReturn(user1).when(userMapper).toEntity(userDto1);
        doReturn(user1.getPassword()).when(passwordEncoder).encode(userDto1.getPassword());
        doReturn(user1).when(userRepository).save(user1);
        Assertions.assertEquals(user1, userService.createUser(userDto1));
    }

    @Test
    void createFriendRequest() {
        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        doReturn(Optional.of(user1)).when(userRepository).findByName(user1.getName());

        User userTo = spy(User.class);
        when(userTo.getId()).thenReturn(2L);

        when(userRepository.findById(userTo.getId())).thenReturn(Optional.of(userTo));

        Assertions.assertNotEquals(user1.getId(), userTo.getId());

        Assertions.assertFalse(user1.getFriends().contains(userTo));

        userTo.addFollower(user1);
        userTo.addFriendRequest(user1);

        Assertions.assertTrue(userTo.getFollowers().contains(user1));
        Assertions.assertTrue(userTo.getFriendRequests().contains(user1));

        Assertions.assertEquals(user1.getId(), userService.createFriendRequest(userTo.getId()));
    }

    @Test
    void acceptFriendRequest() {
        User userSendedRequest = mock(User.class);
        User userAccepted = mock(User.class);

        when(userSendedRequest.getId()).thenReturn(1L);
        when(userAccepted.getId()).thenReturn(2L);
        when(userAccepted.getFriendRequests()).thenReturn(Set.of(userSendedRequest));

        doReturn(userAccepted.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(userAccepted.getName())).thenReturn(Optional.of(userAccepted));
        when(userRepository.findById(userSendedRequest.getId())).thenReturn(Optional.of(userSendedRequest));
        Assertions.assertNotEquals(userSendedRequest.getId(), userAccepted.getId());

        Assertions.assertFalse(userAccepted.getFriends().contains(userSendedRequest));

        Assertions.assertEquals(userAccepted.getId(), userService.acceptFriendRequest(userSendedRequest.getId()));

        when(userAccepted.getFriendRequests()).thenReturn(Collections.emptySet());

        Assertions.assertFalse(userAccepted.getFriendRequests().contains(userSendedRequest));
    }

    @Test
    void refuseFriendRequest() {
        User userRefusedRequest = mock(User.class);
        User userSendedRequest = mock(User.class);

        when(userRefusedRequest.getId()).thenReturn(1L);
        when(userSendedRequest.getId()).thenReturn(2L);
        when(userRefusedRequest.getFriendRequests()).thenReturn(Set.of(userSendedRequest));

        doReturn(userRefusedRequest.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(userRefusedRequest.getName())).thenReturn(Optional.of(userRefusedRequest));
        when(userRepository.findById(userSendedRequest.getId())).thenReturn(Optional.of(userSendedRequest));
        Assertions.assertNotEquals(userRefusedRequest.getId(), userSendedRequest.getId());

        Assertions.assertFalse(userSendedRequest.getFriends().contains(userRefusedRequest));

        Assertions.assertEquals(userRefusedRequest.getId(), userService.refuseFriendRequest(userSendedRequest.getId()));

        when(userRefusedRequest.getFriendRequests()).thenReturn(Collections.emptySet());

        Assertions.assertFalse(userRefusedRequest.getFriendRequests().contains(userSendedRequest));
    }

    @Test
    void refuseFollower() {
        User userRefused = mock(User.class);
        User user = mock(User.class);

        when(userRefused.getId()).thenReturn(1L);
        when(user.getId()).thenReturn(2L);
        when(user.getFollowers()).thenReturn(Set.of(userRefused));

        doReturn(userRefused.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(userRefused.getName())).thenReturn(Optional.of(userRefused));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Assertions.assertNotEquals(userRefused.getId(), user.getId());

        Assertions.assertTrue(user.getFollowers().contains(userRefused));

        Assertions.assertEquals(userRefused.getId(), userService.refuseFollower(user.getId()));

        when(user.getFollowers()).thenReturn(Collections.emptySet());

        Assertions.assertFalse(user.getFollowers().contains(userRefused));
    }

    @Test
    void deleteFriend() {
        User user = mock(User.class);
        User userDeleted = mock(User.class);
        Chat chat = mock(Chat.class);

        when(user.getId()).thenReturn(1L);
        when(userDeleted.getId()).thenReturn(2L);
        when(chat.getId()).thenReturn(1L);

        when(user.getFriends()).thenReturn(Set.of(userDeleted));
        when(user.getChats()).thenReturn(Set.of(chat));
        when(userDeleted.getChats()).thenReturn(Set.of(chat));
        when(chat.getUsers()).thenReturn(Set.of(user, userDeleted));

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(userRepository.findById(userDeleted.getId())).thenReturn(Optional.of(userDeleted));
        Assertions.assertNotEquals(userDeleted.getId(), user.getId());

        Assertions.assertTrue(user.getFriends().contains(userDeleted));

        Assertions.assertTrue(user.getChats().contains(chat));
        Assertions.assertTrue(userDeleted.getChats().contains(chat));

        chatService.deleteChatById(chat.getId());

        Assertions.assertEquals(user.getId(), userService.deleteFriend(userDeleted.getId()));
    }

    @Test
    void getAllUsers() {
        User user = mock(User.class);
        when(user.getName()).thenReturn("Tom");

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.getAllNameAdmins()).thenReturn(List.of("Tom"));
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.toListDto(List.of(user1, user2))).thenReturn(List.of(toUserDto1, toUserDto2));

        Assertions.assertEquals(List.of(toUserDto1, toUserDto2), userService.getAllUsers());
    }

    @Test
    void notGetAllUsers() {
        User user = mock(User.class);
        Mockito.when(user.getName()).thenReturn("Tom");

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.getAllNameAdmins()).thenReturn(List.of("Tom"));
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        Assertions.assertEquals(Collections.emptyList(), userService.getAllUsers());
    }

    @Test
    void getIdUser() {
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
        User user = mock(User.class);
        User userFriend1 = mock(User.class);
        User userFriend3 = mock(User.class);

        when(user.getFriends()).thenReturn(Set.of(userFriend1, userFriend3));

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(userMapper.toListDto(List.of(userFriend1, userFriend3))).thenReturn(List.of(toUserDto1, toUserDto2));

        Assertions.assertEquals(List.of(toUserDto1, toUserDto2), userService.getAllUsersFriends());
    }

    @Test
    void notGetAllUsersFriends() {
        User user = mock(User.class);

        when(user.getFriends()).thenReturn(Collections.emptySet());

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));

        Assertions.assertEquals(Collections.emptyList(), userService.getAllUsersFriends());
    }

    @Test
    void getAllUsersFollowers() {
        User user = mock(User.class);
        User userFollower1 = mock(User.class);
        User userFollower2 = mock(User.class);
        when(user.getFollowers()).thenReturn(Set.of(userFollower1, userFollower2));

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));
        when(userMapper.toListDto(List.of(userFollower1, userFollower2))).thenReturn(List.of(toUserDto1, toUserDto2));

        Assertions.assertEquals(List.of(toUserDto1, toUserDto2), userService.getAllUsersFollowers());
    }

    @Test
    void notGetAllUsersFollowers() {
        User user = mock(User.class);
        when(user.getFollowers()).thenReturn(Collections.emptySet());

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));

        Assertions.assertEquals(Collections.emptyList(), userService.getAllUsersFollowers());
    }
}