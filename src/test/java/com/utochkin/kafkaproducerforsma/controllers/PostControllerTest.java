package com.utochkin.kafkaproducerforsma.controllers;

import com.utochkin.kafkaproducerforsma.config.AppConfig;
import com.utochkin.kafkaproducerforsma.dto.PostDto;
import com.utochkin.kafkaproducerforsma.exceptions.AccessDeniedException;
import com.utochkin.kafkaproducerforsma.exceptions.BadInputDataException;
import com.utochkin.kafkaproducerforsma.exceptions.PostNotFoundException;
import com.utochkin.kafkaproducerforsma.exceptions.UserNotFoundException;
import com.utochkin.kafkaproducerforsma.services.interfaces.PostService;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.MapperFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class)
@ExtendWith(MockitoExtension.class)
@Import(AppConfig.class)
class PostControllerTest {
    private MockMvc mvc;
    @MockBean
    private PostService postService;
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
    void cretePost() throws Exception {
        PostDto postDto = PostDto.builder()
                .description("New description")
                .message("New message")
                .build();

        MockMultipartFile file = new MockMultipartFile("picture_for_test.jpg", "picture_for_test.jpg", "image/jpg", Files.readAllBytes(Paths.get("src/test/resources/picture_for_test.jpg")));

        MockMultipartFile postDtoFile = new MockMultipartFile("postDto", "", "application/json", asJsonString(postDto).getBytes(StandardCharsets.UTF_8));

        PostDto savedPostDto = PostDto.builder()
                .id(1L)
                .description("New description")
                .message("New message")
                .createdAt(LocalDateTime.parse("2024-03-19T16:58:22.014357700"))
                .imageName("UUID + Date + picture_for_test.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg")
                .authorName("Sergey")
                .build();

        when(postService.createPost(any(), any())).thenReturn(savedPostDto);

        mvc.perform(MockMvcRequestBuilders
                        .multipart("/api/v1/post/create")
                        .file(file)
                        .file(postDtoFile))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("New description"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("New message"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value("2024-03-19 16:58"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.imageName").value("UUID + Date + picture_for_test.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.imageLink").value("http://localhost:9000/images/UUID_Date_picture_for_test.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorName").value("Sergey"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void cretePostBadInputDataException() throws Exception {
        PostDto postDto = PostDto.builder()
                .description("New description")
                .message("New message")
                .build();

        MockMultipartFile file = new MockMultipartFile("picture_for_test.jpg", "picture_for_test.jpg", "image/jpg", Files.readAllBytes(Paths.get("src/test/resources/picture_for_test.jpg")));

        MockMultipartFile postDtoFile = new MockMultipartFile("postDto", "", "application/json", asJsonString(postDto).getBytes(StandardCharsets.UTF_8));

        doThrow(new BadInputDataException("Incorrect input file")).when(postService).createPost(any(), any());

        mvc.perform(MockMvcRequestBuilders
                        .multipart("/api/v1/post/create")
                        .file(file)
                        .file(postDtoFile))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Incorrect input file"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPost() throws Exception {
        PostDto savedPostDto = PostDto.builder()
                .id(1L)
                .description("New description")
                .message("New message")
                .createdAt(LocalDateTime.parse("2024-03-19T16:58:22.014357700"))
                .imageName("UUID + Date + picture_for_test.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg")
                .authorName("Sergey")
                .build();

        when(postService.getPost(savedPostDto.getId())).thenReturn(savedPostDto);

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/post/get")
                        .param("postId", String.valueOf(savedPostDto.getId())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("New description"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("New message"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value("2024-03-19 16:58"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.imageName").value("UUID + Date + picture_for_test.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.imageLink").value("http://localhost:9000/images/UUID_Date_picture_for_test.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorName").value("Sergey"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPostNotFound() throws Exception {
        doThrow(new PostNotFoundException()).when(postService).getPost(1L);

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/post/get")
                        .param("postId", String.valueOf(1L)))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Error: post not found!"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updatePost() throws Exception {
        PostDto postDto = PostDto.builder()
                .id(1L)
                .description("New description")
                .message("New message")
                .createdAt(LocalDateTime.parse("2024-03-19T16:58:22.014357700"))
                .authorName("Sergey")
                .build();

        PostDto requestParamPostDto = PostDto.builder()
                .description("Update description")
                .message("Update message")
                .build();

        MockMultipartFile file = new MockMultipartFile("picture_for_test.jpg", "picture_for_test.jpg", "image/jpg", Files.readAllBytes(Paths.get("src/test/resources/picture_for_test.jpg")));

        MockMultipartFile postDtoFile = new MockMultipartFile("postDto", "", "application/json", asJsonString(requestParamPostDto).getBytes(StandardCharsets.UTF_8));

        PostDto updatePostDto = PostDto.builder()
                .id(1L)
                .description("Update description")
                .message("Update message")
                .createdAt(LocalDateTime.parse("2024-03-19T21:33:21.014357700"))
                .imageName("UUID + Date + picture_for_test.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg")
                .authorName("Sergey")
                .build();

        when(postService.updatePost(any(), any(), any())).thenReturn(updatePostDto);

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/api/v1/post/update");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mvc.perform(builder
                        .file(file)
                        .file(postDtoFile)
                        .param("postId", String.valueOf(postDto.getId())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Update description"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Update message"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value("2024-03-19 21:33"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.imageName").value("UUID + Date + picture_for_test.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.imageLink").value("http://localhost:9000/images/UUID_Date_picture_for_test.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorName").value("Sergey"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updatePostBadInputDataException() throws Exception {
        PostDto postDto = PostDto.builder()
                .id(1L)
                .description("New description")
                .message("New message")
                .createdAt(LocalDateTime.parse("2024-03-19T16:58:22.014357700"))
                .authorName("Sergey")
                .build();

        PostDto requestParamPostDto = PostDto.builder()
                .description("Update description")
                .message("Update message")
                .build();

        MockMultipartFile file = new MockMultipartFile("picture_for_test.jpg", "picture_for_test.jpg", "image/jpg", Files.readAllBytes(Paths.get("src/test/resources/picture_for_test.jpg")));

        MockMultipartFile postDtoFile = new MockMultipartFile("postDto", "", "application/json", asJsonString(requestParamPostDto).getBytes(StandardCharsets.UTF_8));

        doThrow(new BadInputDataException("Incorrect input file")).when(postService).updatePost(any(), any(), any());

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/api/v1/post/update");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mvc.perform(builder
                        .file(file)
                        .file(postDtoFile)
                        .param("postId", String.valueOf(postDto.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Incorrect input file"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updatePostAccessDeniedException() throws Exception {
        PostDto postDto = PostDto.builder()
                .id(1L)
                .description("New description")
                .message("New message")
                .createdAt(LocalDateTime.parse("2024-03-19T16:58:22.014357700"))
                .authorName("Sergey")
                .build();

        PostDto requestParamPostDto = PostDto.builder()
                .description("Update description")
                .message("Update message")
                .build();

        MockMultipartFile file = new MockMultipartFile("picture_for_test.jpg", "picture_for_test.jpg", "image/jpg", Files.readAllBytes(Paths.get("src/test/resources/picture_for_test.jpg")));

        MockMultipartFile postDtoFile = new MockMultipartFile("postDto", "", "application/json", asJsonString(requestParamPostDto).getBytes(StandardCharsets.UTF_8));

        doThrow(new AccessDeniedException("Error: access denied!")).when(postService).updatePost(any(), any(), any());

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/api/v1/post/update");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mvc.perform(builder
                        .file(file)
                        .file(postDtoFile)
                        .param("postId", String.valueOf(postDto.getId())))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Error: access denied!"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deletePost() throws Exception {
        PostDto deletedPostDto = PostDto.builder()
                .id(1L)
                .description("New description")
                .message("New message")
                .createdAt(LocalDateTime.parse("2024-03-19T16:58:22.014357700"))
                .imageName("UUID + Date + picture_for_test.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg")
                .authorName("Sergey")
                .build();

        when(postService.deletePost(deletedPostDto.getId())).thenReturn(deletedPostDto.getId());

        mvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/post/delete")
                        .param("postId", String.valueOf(deletedPostDto.getId())))
                .andExpect(status().isOk())
                .andExpect(content().string("Post with id = 1 deleted!"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deletePostAccessDenied() throws Exception {
        PostDto deletedPostDto = PostDto.builder()
                .id(1L)
                .description("New description")
                .message("New message")
                .createdAt(LocalDateTime.parse("2024-03-19T16:58:22.014357700"))
                .imageName("UUID + Date + picture_for_test.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg")
                .authorName("Sergey")
                .build();

        doThrow(new AccessDeniedException("Error: access denied!")).when(postService).deletePost(any());

        mvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/post/delete")
                        .param("postId", String.valueOf(deletedPostDto.getId())))
                .andExpect(status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Error: access denied!"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getFeedUser() throws Exception {
        PostDto postDto1 = PostDto.builder()
                .id(1L)
                .description("Description 1")
                .message("Message 1")
                .createdAt(LocalDateTime.parse("2024-03-19T16:58:22.014357700"))
                .imageName("UUID + Date + picture_for_test1.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test1.jpg")
                .authorName("Sergey")
                .build();

        PostDto postDto2 = PostDto.builder()
                .id(2L)
                .description("Description 2")
                .message("Message 2")
                .createdAt(LocalDateTime.parse("2024-03-20T16:58:22.014357700"))
                .imageName("UUID + Date + picture_for_test2.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test2.jpg")
                .authorName("Ilya")
                .build();

        when(postService.getFeedUser(any())).thenReturn(List.of(postDto1, postDto2));

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/post/getFeedUser")
                        .param("page", String.valueOf(0))
                        .param("size", String.valueOf(2))
                        .param("isSortAsDesk", String.valueOf(true)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].description").value("Description 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].message").value("Message 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].createdAt").value("2024-03-19 16:58"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].imageName").value("UUID + Date + picture_for_test1.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].imageLink").value("http://localhost:9000/images/UUID_Date_picture_for_test1.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].authorName").value("Sergey"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].description").value("Description 2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].message").value("Message 2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].createdAt").value("2024-03-20 16:58"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].imageName").value("UUID + Date + picture_for_test2.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].imageLink").value("http://localhost:9000/images/UUID_Date_picture_for_test2.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].authorName").value("Ilya"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getFeedUserNotFound() throws Exception {
        doThrow(new UserNotFoundException()).when(postService).getFeedUser(any());

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/post/getFeedUser")
                        .param("page", String.valueOf(0))
                        .param("size", String.valueOf(2))
                        .param("isSortAsDesk", String.valueOf(true)))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.messageError").value("Error: user not found!"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllPosts() throws Exception {
        PostDto postDto1 = PostDto.builder()
                .id(1L)
                .description("Description 1")
                .message("Message 1")
                .createdAt(LocalDateTime.parse("2024-03-19T16:58:22.014357700"))
                .imageName("UUID + Date + picture_for_test1.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test1.jpg")
                .authorName("Sergey")
                .build();

        PostDto postDto2 = PostDto.builder()
                .id(2L)
                .description("Description 2")
                .message("Message 2")
                .createdAt(LocalDateTime.parse("2024-03-20T16:58:22.014357700"))
                .imageName("UUID + Date + picture_for_test2.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test2.jpg")
                .authorName("Ilya")
                .build();

        when(postService.getAllPosts()).thenReturn(List.of(postDto1, postDto2));

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/post/getAllPosts"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].description").value("Description 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].message").value("Message 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].createdAt").value("2024-03-19 16:58"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].imageName").value("UUID + Date + picture_for_test1.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].imageLink").value("http://localhost:9000/images/UUID_Date_picture_for_test1.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].authorName").value("Sergey"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].description").value("Description 2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].message").value("Message 2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].createdAt").value("2024-03-20 16:58"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].imageName").value("UUID + Date + picture_for_test2.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].imageLink").value("http://localhost:9000/images/UUID_Date_picture_for_test2.jpg"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].authorName").value("Ilya"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllPostsNotAdmin() throws Exception {
        PostDto postDto1 = PostDto.builder()
                .id(1L)
                .description("Description 1")
                .message("Message 1")
                .createdAt(LocalDateTime.parse("2024-03-19T16:58:22.014357700"))
                .imageName("UUID + Date + picture_for_test1.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test1.jpg")
                .authorName("Sergey")
                .build();

        PostDto postDto2 = PostDto.builder()
                .id(2L)
                .description("Description 2")
                .message("Message 2")
                .createdAt(LocalDateTime.parse("2024-03-20T16:58:22.014357700"))
                .imageName("UUID + Date + picture_for_test2.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test2.jpg")
                .authorName("Ilya")
                .build();

        when(postService.getAllPosts()).thenReturn(List.of(postDto1, postDto2));

        mvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/post/getAllPosts"))
                .andExpect(status().isForbidden());
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