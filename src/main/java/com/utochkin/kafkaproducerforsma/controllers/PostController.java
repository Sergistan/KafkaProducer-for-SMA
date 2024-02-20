package com.utochkin.kafkaproducerforsma.controllers;


import com.utochkin.kafkaproducerforsma.dto.PostDto;
import com.utochkin.kafkaproducerforsma.dto.response.ErrorResponse;
import com.utochkin.kafkaproducerforsma.models.Post;
import com.utochkin.kafkaproducerforsma.services.interfaces.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
@Tag(name = "Post endpoints", description = "Post API")
public class PostController {

    private final PostService postService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Создание поста")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post successful created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequestBody(content = @Content(encoding = @Encoding(name = "postDto", contentType = "application/json")), description = "Ввод описания, сообщения и загрузка картинки для создания поста")
    public ResponseEntity<?> cretePost(@Valid @RequestPart(value = "postDto") PostDto postDto, @RequestPart(value = "file", required = false) MultipartFile file) {
        PostDto savedPost = postService.createPost(postDto, file);
        return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
    }

    @GetMapping("/get")
    @Operation(summary = "Получение поста по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post successful get", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getPost(@NotNull @RequestParam @Parameter(name = "postId", description = "ID поста", in = ParameterIn.QUERY, example = "1") Long postId) {
        return new ResponseEntity<>(postService.getPost(postId), HttpStatus.OK);
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Изменение данных в посте")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post successful changed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequestBody(content = @Content(encoding = @Encoding(name = "postDto", contentType = "application/json")), description = "Ввод описания, сообщения и загрузка картинки для изменения существующего поста")
    public ResponseEntity<?> updatePost(@NotNull @RequestParam @Parameter(name = "postId", description = "ID поста", in = ParameterIn.QUERY, example = "1") Long postId,
                                        @Valid @RequestPart(value = "postDto") PostDto postDto,
                                        @RequestPart(value = "file", required = false) MultipartFile file) {
        PostDto updatePost = postService.updatePost(postId, postDto, file);
        return new ResponseEntity<>(updatePost, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Удаление поста по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post successful deleted", content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject("Post with id = 1 deleted!"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deletePost(@NotNull @RequestParam @Parameter(name = "postId", description = "ID поста", in = ParameterIn.QUERY, example = "1") Long postId) {
        Long idDeletedPost = postService.deletePost(postId);
        return new ResponseEntity<>(String.format("Post with id = %d deleted!", idDeletedPost), HttpStatus.OK);
    }

    @GetMapping("/getFeedUser")
    @Operation(summary = "Получение постов (ленты активности) от пользователей на которых подписан пользователь")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful get last posts followers ", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PostDto.class)))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getFeedUser(@NotNull @RequestParam @Parameter(name = "userId", description = "ID пользователя", in = ParameterIn.QUERY, example = "1") Long userId,
                                         @NotNull @RequestParam(defaultValue = "0") @Parameter(name = "page", description = "ID пользователя для которого будет отображена лента активности",
                                                 in = ParameterIn.QUERY, example = "0") int page,
                                         @NotNull @RequestParam(defaultValue = "2") @Parameter(name = "size", description = "Номер отображаемой страницы ленты активности (начинается с 0)",
                                                 in = ParameterIn.QUERY, example = "2") int size,
                                         @NotNull @RequestParam(defaultValue = "true") @Parameter(name = "isSortAsDesk", description = "Сортировка по времени публикации постов (true - по убыванию, false - по возрастанию)",
                                                 in = ParameterIn.QUERY, example = "true") boolean isSortAsDesk) {
        Sort sortByCreatedAt = isSortAsDesk ? Sort.by("created_at").descending() : Sort.by("created_at");
        Pageable paging = PageRequest.of(page, size, sortByCreatedAt);
        return new ResponseEntity<>(postService.getFeedUser(userId, paging), HttpStatus.OK);
    }

    @GetMapping("/getAllPosts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получение всех постов из базы данных (Доступен только авторизованным пользователям с ролью ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful get all posts from BD ", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PostDto.class)))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getAllPosts() {
        return new ResponseEntity<>(postService.getAllPosts(), HttpStatus.OK);
    }
}