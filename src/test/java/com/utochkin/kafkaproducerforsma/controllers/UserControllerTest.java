package com.utochkin.kafkaproducerforsma.controllers;

import com.utochkin.kafkaproducerforsma.config.AppConfig;
import com.utochkin.kafkaproducerforsma.dto.UserDto;
import com.utochkin.kafkaproducerforsma.exceptions.BadInputDataException;
import com.utochkin.kafkaproducerforsma.exceptions.UserNotFoundException;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@ExtendWith(MockitoExtension.class)
@Import(AppConfig.class)
class UserControllerTest {
    private MockMvc mvc;
    @MockBean
    private UserService userService;
    @MockBean
    private MinioClient minioClient;
    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void beforeTest() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    void createFriendRequest() throws Exception {
        Long userTo = 2L;
        Long userIdFrom = 1L;

        when(userService.createFriendRequest(userTo)).thenReturn(userIdFrom);

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/user/createFriendRequest")
                        .param("userIdTo", String.valueOf(userTo)))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id = 1 send friend request to user with id = 2"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createFriendRequestBadInputDataExceptionRequestToYourself() throws Exception {
        doThrow(new BadInputDataException("You can't send a request to yourself")).when(userService).createFriendRequest(any());

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/user/createFriendRequest")
                        .param("userIdTo", String.valueOf(2L)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("You can't send a request to yourself"));
    }


    @Test
    @WithMockUser(roles = "USER")
    void createFriendRequestBadInputDataExceptionAlreadyHaveFriend() throws Exception {
        doThrow(new BadInputDataException("User with id = 1 already have friend with id = 2")).when(userService).createFriendRequest(any());

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/user/createFriendRequest")
                        .param("userIdTo", String.valueOf(2L)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("User with id = 1 already have friend with id = 2"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void acceptFriendRequest() throws Exception {
        Long userIdSendedRequest = 1L;
        Long userIdAccepted = 2L;

        when(userService.acceptFriendRequest(userIdSendedRequest)).thenReturn(userIdAccepted);

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/user/acceptFriendRequest")
                        .param("userIdSendedRequest", String.valueOf(userIdSendedRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id = 2 accept friend request to user with id = 1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void acceptFriendRequestBadInputDataException() throws Exception {
        doThrow(new BadInputDataException("User with id = 2 already friend with id = 1")).when(userService).acceptFriendRequest(any());

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/user/acceptFriendRequest")
                        .param("userIdSendedRequest", String.valueOf(1L)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("User with id = 2 already friend with id = 1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void refuseFriendRequest() throws Exception {
        Long userIdSendedRequest = 1L;
        Long userRefusedRequest = 2L;

        when(userService.refuseFriendRequest(userIdSendedRequest)).thenReturn(userRefusedRequest);

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/user/refuseFriendRequest")
                        .param("userIdSendedRequest", String.valueOf(userIdSendedRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id = 2 refused friend request to user with id = 1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void refuseFriendRequestBadInputDataException() throws Exception {
        doThrow(new BadInputDataException("User with id = 2 already accept friend request user with id = 1")).when(userService).refuseFriendRequest(any());

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/user/refuseFriendRequest")
                        .param("userIdSendedRequest", String.valueOf(1L)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("User with id = 2 already accept friend request user with id = 1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void refuseFollower() throws Exception {
        Long userId = 2L;
        Long userRefused = 1L;

        when(userService.refuseFollower(userId)).thenReturn(userRefused);

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/user/refuseFollower")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id = 1 refused follower to user with id = 2"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void refuseFollowerBadInputDataException() throws Exception {
        doThrow(new BadInputDataException("User with id = 1 not have follower on id = 2")).when(userService).refuseFollower(any());

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/user/refuseFollower")
                        .param("userId", String.valueOf(2L)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("User with id = 1 not have follower on id = 2"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteFriend() throws Exception {
        Long userIdDeleted = 2L;
        Long userId = 1L;

        when(userService.deleteFriend(userIdDeleted)).thenReturn(userId);

        mvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/user/deleteFriend")
                        .param("userIdDeleted", String.valueOf(userIdDeleted)))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id = 1 delete friend with id = 2"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteFriendBadInputDataException() throws Exception {
        doThrow(new BadInputDataException("User with id = 1 not have friend with id = 2")).when(userService).deleteFriend(any());

        mvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/user/deleteFriend")
                        .param("userIdDeleted", String.valueOf(2L)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("User with id = 1 not have friend with id = 2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers() throws Exception {
        UserDto userDto1 = UserDto.builder()
                .id(1L)
                .name("Sergey")
                .email("sergistan.utochkin@yandex.ru")
                .build();

        UserDto userDto2 = UserDto.builder()
                .id(2L)
                .name("Ilya")
                .email("dzaga73i98@gmail.com")
                .build();

        List<UserDto> userDtoList = List.of(userDto1, userDto2);

        when(userService.getAllUsers()).thenReturn(userDtoList);

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/user/getAllUsers"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value("Sergey"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].email").value("sergistan.utochkin@yandex.ru"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].name").value("Ilya"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].email").value("dzaga73i98@gmail.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsersNotAdmin() throws Exception {
        UserDto userDto1 = UserDto.builder()
                .id(1L)
                .name("Sergey")
                .email("sergistan.utochkin@yandex.ru")
                .build();

        UserDto userDto2 = UserDto.builder()
                .id(2L)
                .name("Ilya")
                .email("dzaga73i98@gmail.com")
                .build();

        List<UserDto> userDtoList = List.of(userDto1, userDto2);

        when(userService.getAllUsers()).thenReturn(userDtoList);

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/user/getAllUsers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getIdUser() throws Exception {
        String username = "Sergey";
        Long userId = 1L;

        when(userService.getIdUser(username)).thenReturn(userId);

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/user/getIdUserByUsername")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(content().string("User id = 1 by username - Sergey"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getIdUserWhenUserNotFound() throws Exception {
        doThrow(new UserNotFoundException()).when(userService).getIdUser(any());

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/user/getIdUserByUsername")
                        .param("username", "Sergey"))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Error: user not found!"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsersFriends() throws Exception {
        UserDto userDto1 = UserDto.builder()
                .id(1L)
                .name("Sergey")
                .email("sergistan.utochkin@yandex.ru")
                .build();

        UserDto userDto2 = UserDto.builder()
                .id(2L)
                .name("Ilya")
                .email("dzaga73i98@gmail.com")
                .build();

        List<UserDto> userDtoList = List.of(userDto1, userDto2);

        when(userService.getAllUsersFriends()).thenReturn(userDtoList);

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/user/getAllUsersFriends"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value("Sergey"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].email").value("sergistan.utochkin@yandex.ru"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].name").value("Ilya"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].email").value("dzaga73i98@gmail.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsersFriendsWhenNotFriend() throws Exception {
        when(userService.getAllUsersFriends()).thenReturn(Collections.emptyList());

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/user/getAllUsersFriends"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsersFollowers() throws Exception {
        UserDto userDto1 = UserDto.builder()
                .id(1L)
                .name("Sergey")
                .email("sergistan.utochkin@yandex.ru")
                .build();

        UserDto userDto2 = UserDto.builder()
                .id(2L)
                .name("Ilya")
                .email("dzaga73i98@gmail.com")
                .build();

        List<UserDto> userDtoList = List.of(userDto1, userDto2);

        when(userService.getAllUsersFollowers()).thenReturn(userDtoList);

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/user/getAllUsersFollowers"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value("Sergey"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].email").value("sergistan.utochkin@yandex.ru"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].name").value("Ilya"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].email").value("dzaga73i98@gmail.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsersFollowersWhenNotFollowers() throws Exception {
        when(userService.getAllUsersFollowers()).thenReturn(Collections.emptyList());

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/user/getAllUsersFollowers"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }
}