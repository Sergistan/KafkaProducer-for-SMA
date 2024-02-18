package com.utochkin.kafkaproducerforsma.controllers;


import com.utochkin.kafkaproducerforsma.dto.response.ErrorResponse;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User endpoints", description = "User API")
public class UserController {

    private final UserService userService;

    @PostMapping("/friendRequest")
    @Operation(summary = "Отправка запроса <добавить в друзья>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request successful post", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("User with id = 1 send friend request to user with id = 2"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> createFriendRequest(@NotNull @RequestParam @Parameter(name = "userIdFrom", description = "ID пользователя отправляющего заявку <добавить в друзья>",
            in = ParameterIn.QUERY, example = "1") Long userIdFrom,
                                                 @NotNull @RequestParam @Parameter(name = "userIdTo", description = "ID пользователя, которому отправили заявку <добавить в друзья>",
                                                         in = ParameterIn.QUERY, example = "2") Long userIdTo) {
        userService.createFriendRequest(userIdFrom, userIdTo);
        return new ResponseEntity<>(String.format("User with id = %s send friend request to user with id = %s",
                userIdFrom, userIdTo), HttpStatus.OK);
    }

    @PostMapping("/acceptFriendRequest")
    @Operation(summary = "Принятие запроса <добавить в друзья>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend successful accept friend request", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("User with id = 2 accept friend request to user with id = 1"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> acceptFriendRequest(@NotNull @RequestParam @Parameter(name = "userIdFrom", description = "ID пользователя отправляющего заявку <добавить в друзья>",
            in = ParameterIn.QUERY, example = "1") Long userIdFrom,
                                                 @NotNull @RequestParam @Parameter(name = "userIdAccepted", description = "ID пользователя принимающего заявку <добавить в друзья>",
                                                         in = ParameterIn.QUERY, example = "2") Long userIdAccepted) {
        userService.acceptFriendRequest(userIdFrom, userIdAccepted);
        return new ResponseEntity<>(String.format("User with id = %s accept friend request to user with id = %s",
                userIdAccepted, userIdFrom), HttpStatus.OK);
    }

    @PostMapping("/refuseFriendRequest")
    @Operation(summary = "Отклонение запроса <добавить в друзья>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request successful refuse", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("User with id = 2 refused friend request to user with id = 1"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> refuseFriendRequest(@NotNull @RequestParam @Parameter(name = "userIdFrom", description = "ID пользователя отправляющего заявку <добавить в друзья>",
            in = ParameterIn.QUERY, example = "1") Long userIdFrom,
                                                 @NotNull @RequestParam @Parameter(name = "userIdRefused", description = "ID пользователя отклонившего заявку <добавить в друзья>",
                                                         in = ParameterIn.QUERY, example = "2") Long userIdRefused) {
        userService.refuseFriendRequest(userIdFrom, userIdRefused);
        return new ResponseEntity<>(String.format("User with id = %s refused friend request to user with id = %s",
                userIdRefused, userIdFrom), HttpStatus.OK);
    }

    @PostMapping("/refuseFollower")
    @Operation(summary = "Отклонение подписки на человека")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend successful refuse follower", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("User with id = 1 refused follower to user with id = 2"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> refuseFollower(@NotNull @RequestParam @Parameter(name = "userIdFollower", description = "ID пользователя, который отказывается от подписки на другого пользователя",
            in = ParameterIn.QUERY, example = "1") Long userIdFollower,
                                            @NotNull @RequestParam @Parameter(name = "userId", description = "ID пользователя, на которого подписан пользователь",
                                                    in = ParameterIn.QUERY, example = "2") Long userId) {
        userService.refuseFollower(userIdFollower, userId);
        return new ResponseEntity<>(String.format("User with id = %s refused follower to user with id = %s",
                userIdFollower, userId), HttpStatus.OK);
    }

    @DeleteMapping("/deleteFriend")
    @Operation(summary = "Удаление пользователя из друзей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend  successful delete friend", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("User with id = 1 delete friend with id = 2"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteFriend(@NotNull @RequestParam @Parameter(name = "userId", description = "ID пользователя, который удаляет из друзей",
            in = ParameterIn.QUERY, example = "1") Long userId,
                                          @NotNull @RequestParam @Parameter(name = "userIdDeleted", description = "ID пользователя, которого удалили из друзей",
                                                  in = ParameterIn.QUERY, example = "2") Long userIdDeleted) {
        userService.deleteFriend(userId, userIdDeleted);
        return new ResponseEntity<>(String.format("User with id = %s delete friend with id = %s",
                userId, userIdDeleted), HttpStatus.OK);
    }

}
