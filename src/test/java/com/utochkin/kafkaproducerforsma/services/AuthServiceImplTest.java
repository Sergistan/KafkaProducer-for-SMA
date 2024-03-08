package com.utochkin.kafkaproducerforsma.services;

import com.utochkin.kafkaproducerforsma.dto.request.JwtRequest;
import com.utochkin.kafkaproducerforsma.dto.response.JwtResponse;
import com.utochkin.kafkaproducerforsma.exceptions.AccessDeniedException;
import com.utochkin.kafkaproducerforsma.exceptions.BadCredentialsException;
import com.utochkin.kafkaproducerforsma.models.Role;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.security.JwtTokenProvider;
import com.utochkin.kafkaproducerforsma.services.impl.AuthServiceImpl;
import com.utochkin.kafkaproducerforsma.services.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserServiceImpl userService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private User user1;
    private JwtRequest jwtRequest;
    private JwtResponse jwtResponse;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .build();

        jwtRequest = JwtRequest.builder()
                .name("Sergey")
                .password("111")
                .build();

        jwtResponse = new JwtResponse(user1.getId(), user1.getName(), "TOKEN_USER1", "REFRESH_TOKEN_USER1");

        usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(jwtRequest.getName(), jwtRequest.getPassword());
    }


    @Test
    void login() {
        doReturn(user1).when(userService).findByName(user1.getName());
        doReturn("TOKEN_USER1").when(jwtTokenProvider).createAccessToken(user1.getId(), user1.getName(), user1.getRole());
        doReturn("REFRESH_TOKEN_USER1").when(jwtTokenProvider).createRefreshToken(user1.getId(), user1.getName());
        Assertions.assertEquals(jwtResponse, authService.login(jwtRequest));
        verify(authenticationManager, Mockito.times(1)).authenticate(usernamePasswordAuthenticationToken);
    }

    @Test
    void incorrectNameLogin() {
        JwtRequest incorrectJwtRequestName = JwtRequest.builder()
                .name("S")
                .password("111")
                .build();
        usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(incorrectJwtRequestName.getName(), incorrectJwtRequestName.getPassword());
        doThrow(BadCredentialsException.class).when(authenticationManager).authenticate(usernamePasswordAuthenticationToken);
        Assertions.assertThrows(BadCredentialsException.class, () -> authService.login(incorrectJwtRequestName));
    }

    @Test
    void incorrectNamePassword() {
        JwtRequest incorrectJwtRequestPassword = JwtRequest.builder()
                .name("Sergey")
                .password("1")
                .build();
        usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(incorrectJwtRequestPassword.getName(), incorrectJwtRequestPassword.getPassword());
        doThrow(BadCredentialsException.class).when(authenticationManager).authenticate(usernamePasswordAuthenticationToken);
        Assertions.assertThrows(BadCredentialsException.class, () -> authService.login(incorrectJwtRequestPassword));
    }

    @Test
    void refresh() {
        JwtResponse refreshJwtResponse = new JwtResponse(user1.getId(), user1.getName(), "TOKEN_USER1", "NEW_REFRESH_TOKEN_USER1");
        doReturn(refreshJwtResponse).when(jwtTokenProvider).refreshUserTokens("REFRESH_TOKEN_USER1");
        Assertions.assertEquals(refreshJwtResponse, authService.refresh("REFRESH_TOKEN_USER1"));
    }

    @Test
    void refreshAccessDenied() {
        doThrow(AccessDeniedException.class).when(jwtTokenProvider).refreshUserTokens(anyString());
        Assertions.assertThrows(AccessDeniedException.class, () -> authService.refresh(anyString()));
    }

}