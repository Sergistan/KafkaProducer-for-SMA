package com.utochkin.kafkaproducerforsma.controllers;


import com.utochkin.kafkaproducerforsma.dto.UserDto;
import com.utochkin.kafkaproducerforsma.dto.request.JwtRequest;
import com.utochkin.kafkaproducerforsma.dto.response.ErrorResponse;
import com.utochkin.kafkaproducerforsma.dto.response.JwtResponse;
import com.utochkin.kafkaproducerforsma.mappers.UserMapper;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.services.interfaces.AuthService;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication endpoints", description = "Authentication API")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    @Operation(summary = "Аутентификация пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful Auth", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> login(@RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = JwtRequest.class),
            encoding = @Encoding(name = "jwtResponse", contentType = "application/json")), description = "Ввод имени и пароля пользователя для прохождения аутентификации", required = true) @Valid JwtRequest jwtRequest) {
        JwtResponse login = authService.login(jwtRequest);
        return new ResponseEntity<>(login, HttpStatus.OK);
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful registration", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> register(@RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = UserDto.class),
            encoding = @Encoding(name = "userDto", contentType = "application/json")), description = "Ввод имени, пароля и электронной почты для регистрации пользователя (Имя должно быть уникальным)",
            required = true) UserDto userDto) {
        User createdUser = userService.createUser(userDto);
        UserDto userDtoCreated = userMapper.toDto(createdUser);
        return new ResponseEntity<>(userDtoCreated, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновление токена аутентификации и токена обновления")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful refresh token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> refresh(@NotNull @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = String.class),
            examples = @ExampleObject("$2a$12$5lWa/FKZ925t3p.QDvj/oeo38ot7Lna9vhhiCYCOBv8YV0h2uN/5q"), encoding = @Encoding(name = "refreshToken", contentType = "application/json")),
            description = "Обновление токена аутентификации и токена обновления (необходимо ввести текущий refreshToken)", required = true)  String refreshToken) {
        JwtResponse refresh = authService.refresh(refreshToken);
        return new ResponseEntity<>(refresh, HttpStatus.OK);
    }
}
