package com.utochkin.kafkaproducerforsma.services;

import com.utochkin.kafkaproducerforsma.dto.PostDto;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.services.impl.PostServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class PostServiceCachingTest {
    // Для включения тестов необходимо запустить docker-compose, предварительно закомментировав сервисы: producer_app, consumer_app, db, pgadmin и поменять в файле application.yaml значение в data-redis-host с REDIS_HOST
// на REDIS_HOST_FROM_LOCAL_UP. После теста на удаление кэша запись из БД удаляется, поэтому ля повторного запуска тестов необходимо вручную исполнить файл create_post_for_test.sql
    @Autowired
    private PostServiceImpl postService;
    @Autowired
    private CacheManager cacheManager;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @Test
    void getPost() {
        Cache cache = cacheManager.getCache("PostService::getPost");

        assertNull(cache.get("postId"));

        PostDto result1 = postService.getPost(1L);

        Cache.ValueWrapper valueWrapper = Objects.requireNonNull(cacheManager.getCache("PostService::getPost")).get(1L);
        PostDto resultFromCache = (PostDto) valueWrapper.get();

        assertEquals(resultFromCache, result1);

        PostDto result2 = postService.getPost(1L);

        assertEquals(resultFromCache, result2);

        assertEquals(result1, result2);
    }

    @Test
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

        PostDto result1 = postService.getPost(1L);

        Cache.ValueWrapper valueWrapper = Objects.requireNonNull(cacheManager.getCache("PostService::getPost")).get(1L);
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

        PostDto postDto = postService.updatePost(1L, updatePostDto, multipartFile);

        Cache.ValueWrapper updateValueWrapper = Objects.requireNonNull(cacheManager.getCache("PostService::getPost")).get(1L);
        PostDto updateResultFromCache = (PostDto) updateValueWrapper.get();

        assertEquals(updateResultFromCache, postDto);
        assertNotEquals(resultFromCache, updateResultFromCache);
    }

    @Test
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

        PostDto result1 = postService.getPost(1L);

        Cache.ValueWrapper valueWrapper = Objects.requireNonNull(cacheManager.getCache("PostService::getPost")).get(1L);
        PostDto resultFromCache = (PostDto) valueWrapper.get();

        assertEquals(resultFromCache, result1);

        postService.deletePost(1L);

        assertNull(cache.get("postId"));
    }

}
