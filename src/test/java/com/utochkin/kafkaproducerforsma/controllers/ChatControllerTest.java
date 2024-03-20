package com.utochkin.kafkaproducerforsma.controllers;

import com.utochkin.kafkaproducerforsma.config.AppConfig;
import com.utochkin.kafkaproducerforsma.dto.ChatDto;
import com.utochkin.kafkaproducerforsma.dto.MessageDto;
import com.utochkin.kafkaproducerforsma.exceptions.AccessDeniedException;
import com.utochkin.kafkaproducerforsma.exceptions.BadInputDataException;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.services.interfaces.ChatService;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.MapperFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChatController.class)
@ExtendWith(MockitoExtension.class)
@Import(AppConfig.class)
class ChatControllerTest {
    private MockMvc mvc;
    @MockBean
    private ChatService chatService;
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
    void getChat() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), user2.getId(), Set.of(user1, user2));

        when(chatService.getChatById(chatDto.getId())).thenReturn(chatDto);

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/chat/get")
                        .param("chatId", String.valueOf(chatDto.getId())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value("2024-03-19 16:58"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastMessage").value("Msg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstUserId").value(user1.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.secondUserId").value(user2.getId()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getChatAccessDenied() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), user2.getId(), Set.of(user1, user2));

        doThrow(new AccessDeniedException("Error: access denied!")).when(chatService).getChatById(any());

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/chat/get")
                        .param("chatId", String.valueOf(chatDto.getId())))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Error: access denied!"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getLastMessageFromChat() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), user2.getId(), Set.of(user1, user2));

        when(chatService.getLastMessage(chatDto.getId())).thenReturn(chatDto.getLastMessage());

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/chat/lastMessage")
                        .param("chatId", String.valueOf(chatDto.getId())))
                .andExpect(status().isOk())
                .andExpect(content().string("Last message at the chat: Msg"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getLastMessageFromChatAccessDenied() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), user2.getId(), Set.of(user1, user2));

        doThrow(new AccessDeniedException("Error: access denied!")).when(chatService).getLastMessage(any());

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/chat/lastMessage")
                        .param("chatId", String.valueOf(chatDto.getId())))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Error: access denied!"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createChat() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDtoRequest = new ChatDto(null, null, null, user1.getId(), user2.getId(), null);

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), user2.getId(), Set.of(user1, user2));

        when(chatService.createChat(chatDtoRequest)).thenReturn(chatDto);

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/chat/create")
                        .content(asJsonString(chatDtoRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("Chat with id = 1 created between user with id = 1 and user with id = 2"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createChatAccessDenied() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDtoRequest = new ChatDto(null, null, null, user1.getId(), user2.getId(), null);

        doThrow(new AccessDeniedException("Error: access denied!")).when(chatService).createChat(any());

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/chat/create")
                        .content(asJsonString(chatDtoRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Error: access denied!"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createChatBadInputDataException() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDtoRequest = new ChatDto(null, null, null, user1.getId(), user2.getId(), null);

        doThrow(new BadInputDataException("These users already have a chat")).when(chatService).createChat(any());

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/chat/create")
                        .content(asJsonString(chatDtoRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("These users already have a chat"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteChat() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), user2.getId(), Set.of(user1, user2));

        mvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/chat/delete")
                        .param("chatId", String.valueOf(chatDto.getId())))
                .andExpect(status().isOk())
                .andExpect(content().string("Chat with id = 1 deleted!"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteChatAccessDenied() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), user2.getId(), Set.of(user1, user2));

        doThrow(new AccessDeniedException("Error: access denied!")).when(chatService).deleteChatById(any());

        mvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/chat/delete")
                        .param("chatId", String.valueOf(chatDto.getId())))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Error: access denied!"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void joinChat() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), null, Set.of(user1));

        when(chatService.joinChat(chatDto.getId())).thenReturn(user2.getId());

        mvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/chat/join")
                        .param("chatId", String.valueOf(chatDto.getId())))
                .andExpect(status().isAccepted())
                .andExpect(content().string("User with id = 2 join to chat with id = 1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void joinChatAccessDenied() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), null, Set.of(user1));

        doThrow(new AccessDeniedException("Error: access denied!")).when(chatService).joinChat(any());

        mvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/chat/join")
                        .param("chatId", String.valueOf(chatDto.getId())))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Error: access denied!"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void joinChatBadInputDataException() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), null, Set.of(user1));

        doThrow(new BadInputDataException("There are already two users in the chat")).when(chatService).joinChat(any());

        mvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/chat/join")
                        .param("chatId", String.valueOf(chatDto.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("There are already two users in the chat"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void leaveChat() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), user2.getId(), Set.of(user1, user2));

        when(chatService.leaveChat(chatDto.getId())).thenReturn(user1.getId());

        mvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/chat/leave")
                        .param("chatId", String.valueOf(chatDto.getId())))
                .andExpect(status().isAccepted())
                .andExpect(content().string("User with id = 1 leave to chat with id = 1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void leaveChatAccessDenied() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), user2.getId(), Set.of(user1, user2));

        doThrow(new AccessDeniedException("Error: access denied!")).when(chatService).leaveChat(any());

        mvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/chat/leave")
                        .param("chatId", String.valueOf(chatDto.getId())))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Error: access denied!"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllChats() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        User user3 = User.builder()
                .id(3L)
                .name("Alina")
                .build();

        ChatDto chatDto1 = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg1", user1.getId(), user2.getId(), Set.of(user1, user2));
        ChatDto chatDto2 = new ChatDto(2L, LocalDateTime.parse("2024-03-20T16:58:22.014357700"), "Msg2", user2.getId(), user3.getId(), Set.of(user2, user3));

        when(chatService.getAllChats()).thenReturn(List.of(chatDto1, chatDto2));

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/chat/getAllChats"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].createdAt").value("2024-03-19 16:58"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].lastMessage").value("Msg1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].firstUserId").value(user1.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].secondUserId").value(user2.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].createdAt").value("2024-03-20 16:58"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].lastMessage").value("Msg2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].firstUserId").value(user2.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].secondUserId").value(user3.getId()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllChatsNotAdmin() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        User user3 = User.builder()
                .id(3L)
                .name("Alina")
                .build();

        ChatDto chatDto1 = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg1", user1.getId(), user2.getId(), Set.of(user1, user2));
        ChatDto chatDto2 = new ChatDto(2L, LocalDateTime.parse("2024-03-20T16:58:22.014357700"), "Msg2", user2.getId(), user3.getId(), Set.of(user2, user3));

        when(chatService.getAllChats()).thenReturn(List.of(chatDto1, chatDto2));

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/chat/getAllChats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllMessagesInChat() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), user2.getId(), Set.of(user1, user2));

        MessageDto msg1Dto = new MessageDto(1L, 1L, "1 MSG", user1.getName(), LocalDateTime.parse("2024-03-19T16:58:22.014357700"));
        MessageDto msg2Dto = new MessageDto(2L, 1L, "2 MSG", user2.getName(), LocalDateTime.parse("2024-03-19T16:59:22.014357700"));

        when(chatService.getAllMessagesInChat(chatDto.getId())).thenReturn(List.of(msg1Dto,msg2Dto));

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/chat/getAllMessagesInChat")
                        .param("chatId", String.valueOf(chatDto.getId())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].chatId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].text").value("1 MSG"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].senderName").value(user1.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].sentAt").value("2024-03-19 16:58"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].chatId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].text").value("2 MSG"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].senderName").value(user2.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].sentAt").value("2024-03-19 16:59"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllMessagesInChatAccessDenied() throws Exception {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .build();

        ChatDto chatDto = new ChatDto(1L, LocalDateTime.parse("2024-03-19T16:58:22.014357700"), "Msg", user1.getId(), user2.getId(), Set.of(user1, user2));

        doThrow(new AccessDeniedException("Error: access denied!")).when(chatService).getAllMessagesInChat(any());

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/chat/getAllMessagesInChat")
                        .param("chatId", String.valueOf(chatDto.getId())))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Error: access denied!"));
    }

    public static String asJsonString(final Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.USE_ANNOTATIONS);
            return mapper.writeValueAsString(obj);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}