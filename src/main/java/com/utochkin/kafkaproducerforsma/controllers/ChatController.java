package com.utochkin.kafkaproducerforsma.controllers;


import com.utochkin.kafkaproducerforsma.dto.ChatDto;
import com.utochkin.kafkaproducerforsma.dto.response.ErrorResponse;
import com.utochkin.kafkaproducerforsma.services.interfaces.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat endpoints", description = "Chat API")
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/get")
    @Operation(summary = "Получение чата по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful get chat", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getChat(@NotNull @RequestParam @Parameter(name = "chatId", description = "ID чата", in = ParameterIn.QUERY, example = "1") Long chatId) {
        return new ResponseEntity<>(chatService.getChatById(chatId), HttpStatus.OK);
    }

    @GetMapping("/last_message/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получение последнего сообщения в чате по id чата (Доступен только авторизованным пользователям с ролью ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful get last message at chat", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("Last message at the chat: ***"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not forbidden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getLastMessageFromChat(@NotNull @RequestParam @Parameter(name = "chatId", description = "ID чата", in = ParameterIn.QUERY, example = "1") Long chatId) {
        String lastMessage = chatService.getLastMessage(chatId);
        return new ResponseEntity<>(String.format("Last message at the chat: %s", lastMessage), HttpStatus.OK);
    }

    @PostMapping("/create")
    @Operation(summary = "Создание чата")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful create chat", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("Chat with id = 1 created between user with id = 1 and user with id = 2"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> createChat(@RequestBody @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = ChatDto.class),
            encoding = @Encoding(name = "chatDto", contentType = "application/json")), description = "Ввод id первого и второго пользователей для создания чата между ними", required = true) ChatDto chatDto) {
        ChatDto chatCreated = chatService.createChat(chatDto);
        return new ResponseEntity<>(String.format("Chat with id = %s created between user with id = %s and user with id = %s", chatCreated.getId(), chatDto.getFirstUserId(), chatDto.getSecondUserId()), HttpStatus.CREATED);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Удаление чата по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful delete chat", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("Chat with id = 1 deleted!"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteChat(@NotNull @RequestParam @Parameter(name = "chatId", description = "ID чата", in = ParameterIn.QUERY, example = "1") Long chatId) {
        chatService.deleteChatById(chatId);
        return new ResponseEntity<>(String.format("Chat with id = %d deleted!", chatId), HttpStatus.OK);
    }

    @PutMapping("/join")
    @Operation(summary = "Присоединение к чату по id чата и по id пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Successful join into chat", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("User with id = 1 join to chat with id = 1"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> joinChat(@NotNull @RequestParam(value = "chatId") @Parameter(name = "chatId", description = "ID чата", in = ParameterIn.QUERY, example = "1") Long chatId,
                                      @NotNull @RequestParam(value = "userId") @Parameter(name = "userId", description = "ID пользователя", in = ParameterIn.QUERY, example = "1") Long userId) {
        chatService.joinChat(userId, chatId);
        return new ResponseEntity<>(String.format("User with id = %s join to chat with id = %s", userId, chatId), HttpStatus.ACCEPTED);
    }

    @PutMapping("/leave")
    @Operation(summary = "Отключение от чата по id чата и по id пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Successful leave from chat", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("User with id = 1 leave to chat with id = 1"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> leaveChat(@NotNull @RequestParam(value = "chatId") @Parameter(name = "chatId", description = "ID чата", in = ParameterIn.QUERY, example = "1") Long chatId,
                                       @NotNull @RequestParam(value = "userId") @Parameter(name = "userId", description = "ID пользователя", in = ParameterIn.QUERY, example = "1") Long userId) {
        chatService.leaveChat(userId, chatId);
        return new ResponseEntity<>(String.format("User with id = %s leave to chat with id = %s", userId, chatId), HttpStatus.ACCEPTED);
    }

}
