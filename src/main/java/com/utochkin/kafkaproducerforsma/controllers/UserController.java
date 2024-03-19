package com.utochkin.kafkaproducerforsma.controllers;


import com.utochkin.kafkaproducerforsma.dto.UserDto;
import com.utochkin.kafkaproducerforsma.dto.response.ErrorResponse;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User endpoints", description = "User API")
public class UserController {

    private final UserService userService;

    @PostMapping("/createFriendRequest")
    @Operation(summary = "Отправка запроса <добавить в друзья>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request successful post", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("User with id = 1 send friend request to user with id = 2"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> createFriendRequest(@NotNull @RequestParam @Parameter(name = "userIdTo", description = "ID пользователя, которому отправляем заявку <добавить в друзья>",
                                                         in = ParameterIn.QUERY, example = "2") Long userIdTo) {
        Long userIdFrom = userService.createFriendRequest(userIdTo);
        return new ResponseEntity<>(String.format("User with id = %s send friend request to user with id = %s", userIdFrom, userIdTo), HttpStatus.OK);
    }

    @PostMapping("/acceptFriendRequest")
    @Operation(summary = "Принятие запроса <добавить в друзья>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend successful accept friend request", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("User with id = 2 accept friend request to user with id = 1"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> acceptFriendRequest(@NotNull @RequestParam @Parameter(name = "userIdSendedRequest", description = "ID пользователя отправившего заявку <добавить в друзья>",
            in = ParameterIn.QUERY, example = "1") Long userIdSendedRequest){
        Long userIdAccepted = userService.acceptFriendRequest(userIdSendedRequest);
        return new ResponseEntity<>(String.format("User with id = %s accept friend request to user with id = %s", userIdAccepted, userIdSendedRequest), HttpStatus.OK);
    }

    @PostMapping("/refuseFriendRequest")
    @Operation(summary = "Отклонение запроса <добавить в друзья>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request successful refuse", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("User with id = 2 refused friend request to user with id = 1"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> refuseFriendRequest(@NotNull @RequestParam @Parameter(name = "userIdSendedRequest", description = "ID пользователя отправившего заявку <добавить в друзья>",
            in = ParameterIn.QUERY, example = "1") Long userIdSendedRequest) {
        Long userIdRefusedRequest = userService.refuseFriendRequest(userIdSendedRequest);
        return new ResponseEntity<>(String.format("User with id = %s refused friend request to user with id = %s", userIdRefusedRequest, userIdSendedRequest), HttpStatus.OK);
    }

    @PostMapping("/refuseFollower")
    @Operation(summary = "Отклонение подписки на человека")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend successful refuse follower", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("User with id = 1 refused follower to user with id = 2"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> refuseFollower(@NotNull @RequestParam @Parameter(name = "userId", description = "ID пользователя, на которого подписан пользователь",
                                                    in = ParameterIn.QUERY, example = "2") Long userId) {
        Long userIdRefused = userService.refuseFollower(userId);
        return new ResponseEntity<>(String.format("User with id = %s refused follower to user with id = %s", userIdRefused, userId), HttpStatus.OK);
    }

    @DeleteMapping("/deleteFriend")
    @Operation(summary = "Удаление пользователя из друзей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend  successful delete friend", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("User with id = 1 delete friend with id = 2"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteFriend(@NotNull @RequestParam @Parameter(name = "userIdDeleted", description = "ID пользователя, которого хотят удалить из друзей",
                                                  in = ParameterIn.QUERY, example = "2") Long userIdDeleted) {
        Long userId = userService.deleteFriend(userIdDeleted);
        return new ResponseEntity<>(String.format("User with id = %s delete friend with id = %s", userId, userIdDeleted), HttpStatus.OK);
    }

    @GetMapping("/getAllUsers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получение всех пользователей из базы данных (Доступен только авторизованным пользователям с ролью ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful get all users from BD", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not forbidden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @GetMapping("/getIdUserByUsername")
    @Operation(summary = "Получение id пользователя по его имени")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful get id user", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getIdUser(@NotNull @RequestParam @Parameter(name = "username", description = "Имя пользователя, по которому ищется userId",
            in = ParameterIn.QUERY, example = "Sergey") String username) {
         Long userId = userService.getIdUser(username);
        return new ResponseEntity<>(String.format("User id = %s by username - %s", userId, username), HttpStatus.OK);
    }

    @GetMapping("/getAllUsersFriends")
    @Operation(summary = "Получение всех друзей пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful get all friends user", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getAllUsersFriends() {
        return new ResponseEntity<>(userService.getAllUsersFriends(), HttpStatus.OK);
    }

    @GetMapping("/getAllUsersFollowers")
    @Operation(summary = "Получение всех фолловеров пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful get all followers user", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getAllUsersFollowers() {
        return new ResponseEntity<>(userService.getAllUsersFollowers(), HttpStatus.OK);
    }
}
