package com.utochkin.kafkaproducerforsma.services;

import com.utochkin.kafkaproducerforsma.dto.PostDto;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.services.impl.PostServiceImpl;
import com.utochkin.kafkaproducerforsma.utils.BaseSpringTestFull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostServiceCachingTest extends BaseSpringTestFull {

    @Autowired
    private PostServiceImpl postService;
    @Autowired
    private CacheManager cacheManager;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @Test
    @Sql("/create_post_for_test.sql")
    @Order(1)
    void getPost() {
        Cache cache = cacheManager.getCache("PostService::getPost");

        assertNull(cache.get("postId"));

        PostDto result1 = postService.getPost(1111L);

        Cache.ValueWrapper valueWrapper = Objects.requireNonNull(cacheManager.getCache("PostService::getPost")).get(1111L);
        PostDto resultFromCache = (PostDto) valueWrapper.get();

        assertEquals(resultFromCache, result1);

        PostDto result2 = postService.getPost(1111L);

        assertEquals(resultFromCache, result2);

        assertEquals(result1, result2);
    }

    @Test
    @Order(2)
    void updatePost() throws IOException {
        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        Cache cache = cacheManager.getCache("PostService::getPost");

        assertNull(cache.get("postId"));

        PostDto result1 = postService.getPost(1111L);

        Cache.ValueWrapper valueWrapper = Objects.requireNonNull(cacheManager.getCache("PostService::getPost")).get(1111L);
        PostDto resultFromCache = (PostDto) valueWrapper.get();

        assertEquals(resultFromCache, result1);

        PostDto updatePostDto = PostDto.builder()
                .description("Update description")
                .message("Update message")
                .build();

        Path path = Paths.get("src/test/resources/picture_for_test.jpg");
        String name = "picture_for_test.jpg";
        String originalFileName = "picture_for_test.jpg";
        String contentType = "image/jpg";
        byte[] content = Files.readAllBytes(path);

        MultipartFile multipartFile = new MockMultipartFile(name, originalFileName, contentType, content);

        PostDto postDto = postService.updatePost(1111L, updatePostDto, multipartFile);

        Cache.ValueWrapper updateValueWrapper = Objects.requireNonNull(cacheManager.getCache("PostService::getPost")).get(1111L);
        PostDto updateResultFromCache = (PostDto) updateValueWrapper.get();

        assertEquals(updateResultFromCache, postDto);
        assertNotEquals(resultFromCache, updateResultFromCache);
    }

    @Test
    @Order(3)
    void deletePost() {
        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .build();

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        Cache cache = cacheManager.getCache("PostService::getPost");

        assertNull(cache.get("postId"));

        PostDto result1 = postService.getPost(1111L);

        Cache.ValueWrapper valueWrapper = Objects.requireNonNull(cacheManager.getCache("PostService::getPost")).get(1111L);
        PostDto resultFromCache = (PostDto) valueWrapper.get();

        assertEquals(resultFromCache, result1);

        postService.deletePost(1111L);

        assertNull(cache.get("postId"));
    }
}
