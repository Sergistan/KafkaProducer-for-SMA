package com.utochkin.kafkaproducerforsma.controllers;

import com.utochkin.kafkaproducerforsma.config.AppConfig;
import com.utochkin.kafkaproducerforsma.dto.UserDto;
import com.utochkin.kafkaproducerforsma.dto.request.JwtRequest;
import com.utochkin.kafkaproducerforsma.dto.response.JwtResponse;
import com.utochkin.kafkaproducerforsma.exceptions.AccessDeniedException;
import com.utochkin.kafkaproducerforsma.exceptions.BadCredentialsException;
import com.utochkin.kafkaproducerforsma.exceptions.controllerAdvice.ExceptionControllerAdvice;
import com.utochkin.kafkaproducerforsma.mappers.UserMapper;
import com.utochkin.kafkaproducerforsma.models.Role;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.services.impl.AuthServiceImpl;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.MapperFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(classes = {AppConfig.class})
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@Import(AuthController.class)
class AuthControllerTest {
    private MockMvc mvc;
    @Autowired
    private AuthController authController;
    @MockBean
    private AuthServiceImpl authService;
    @MockBean
    private UserService userService;
    @MockBean
    private UserMapper userMapper;
    @MockBean
    private MinioClient minioClient;
    @MockBean
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void beforeTest() {
        mvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new ExceptionControllerAdvice())
                .build();
    }

    @Test
    void login() throws Exception {
        JwtRequest jwtRequest = JwtRequest.builder().name("Sergey").password("111").build();

        JwtResponse jwtResponse = new JwtResponse(1L, jwtRequest.getName(), "TOKEN_USER1", "REFRESH_TOKEN_USER1");

        when(authService.login(jwtRequest)).thenReturn(jwtResponse);

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
                        .content(asJsonString(jwtRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Sergey"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value("TOKEN_USER1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").value("REFRESH_TOKEN_USER1"))
                .andDo(print());
    }

    @Test
    void loginBadCredentials() throws Exception {
        JwtRequest jwtRequest = JwtRequest.builder().name("S").password("1").build();

        doThrow(new BadCredentialsException()).when(authService).login(any());

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
                        .content(asJsonString(jwtRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Incorrect username and/or password"))
                .andDo(print());
    }

    @Test
    void register() throws Exception {
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

        UserDto userDtoCreated = UserDto.builder()
                .id(1L)
                .name("Sergey")
                .email("sergistan.utochkin@yandex.ru")
                .build();

        when(userService.createUser(any())).thenReturn(user);
        when(userMapper.toDto(any())).thenReturn(userDtoCreated);

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/auth/register")
                        .content(asJsonString(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Sergey"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("sergistan.utochkin@yandex.ru"))
                .andDo(print());
    }

    @Test
    void registerBadCredentials() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Sergey")
                .password("111")
                .email("sergistan.utochkin@yandex.ru")
                .build();

        doThrow(new BadCredentialsException()).when(userService).createUser(any());

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/auth/register")
                        .content(asJsonString(userDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Incorrect username and/or password"))
                .andDo(print());
    }

    @Test
    void refresh() throws Exception {
        JwtResponse refreshJwtResponse = new JwtResponse(1L, "Sergey", "TOKEN_USER1", "NEW_REFRESH_TOKEN_USER1");

        when(authService.refresh("REFRESH_TOKEN_USER1")).thenReturn(refreshJwtResponse);

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/auth/refresh")
                        .content("REFRESH_TOKEN_USER1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Sergey"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value("TOKEN_USER1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").value("NEW_REFRESH_TOKEN_USER1"))
                .andDo(print());
    }

    @Test
    void refreshAccessDenied() throws Exception {
        doThrow(new AccessDeniedException("Access denied")).when(authService).refresh(anyString());

        mvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/auth/refresh")
                        .content("REFRESH_TOKEN_USER1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Access denied"))
                .andDo(print());
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