package com.utochkin.kafkaproducerforsma.services;

import com.utochkin.kafkaproducerforsma.dto.PostDto;
import com.utochkin.kafkaproducerforsma.exceptions.AccessDeniedException;
import com.utochkin.kafkaproducerforsma.exceptions.BadInputDataException;
import com.utochkin.kafkaproducerforsma.exceptions.PostNotFoundException;
import com.utochkin.kafkaproducerforsma.mappers.PostMapper;
import com.utochkin.kafkaproducerforsma.models.Post;
import com.utochkin.kafkaproducerforsma.models.Role;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.props.MinioProperties;
import com.utochkin.kafkaproducerforsma.repository.PostRepository;
import com.utochkin.kafkaproducerforsma.repository.UserRepository;
import com.utochkin.kafkaproducerforsma.sender.KafkaSenderService;
import com.utochkin.kafkaproducerforsma.services.impl.PostServiceImpl;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostServiceImplTest {
    @InjectMocks
    private PostServiceImpl postService;
    @Mock
    private PostMapper postMapper;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MinioClient minioClient;
    @Mock
    private MinioProperties minioProperties;
    @Mock
    private MultipartFile multipartFile;
    @Mock
    private KafkaSenderService kafkaSenderService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private PageRequest pageRequest;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void createPostWithMultipartFile() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        PostDto postDto = PostDto.builder()
                .description("New description")
                .message("New message")
                .build();

        Post post = Post.builder()
                .id(1L)
                .description("New description")
                .message("New message")
                .createdAt(LocalDateTime.now())
                .build();

        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .build();

        when(multipartFile.getInputStream()).thenReturn(new FileInputStream("src/test/resources/picture_for_test.jpg"));

        when(postMapper.toEntity(postDto)).thenReturn(post);

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));

        Assertions.assertFalse(multipartFile.isEmpty());

        when(minioProperties.getBucket()).thenReturn("images");
        when(minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build())).thenReturn(true);

        when(multipartFile.getContentType()).thenReturn(Files.probeContentType(Path.of("src/test/resources/picture_for_test.jpg")));

        when(multipartFile.getOriginalFilename()).thenReturn("UUID + Date + picture_for_test.jpg");
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/picture_for_test.jpg");

        when(minioClient.putObject(PutObjectArgs.builder().stream(fileInputStream, fileInputStream.available(), -1)
                .bucket(minioProperties.getBucket()).object("UUID + Date + picture_for_test.jpg").build()))
                .thenReturn(new ObjectWriteResponse(null, "images", null, "UUID + Date + picture_for_test.jpg", null, null));

        when(minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(minioProperties.getBucket())
                .object("UUID + Date + picture_for_test.jpg")
                .build())).thenReturn("http://localhost:9000/images/UUID_Date_picture_for_test.jpg");

        post.setImageName("UUID + Date + picture_for_test.jpg");
        post.setImageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg");
        post.setUser(user);

        PostDto postDtoWithPicture = PostDto.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName("UUID + Date + picture_for_test.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg")
                .build();

        when(postMapper.toDto(post)).thenReturn(postDtoWithPicture);

        Assertions.assertEquals(postDtoWithPicture, postService.createPost(postDto, multipartFile));
        verify(postRepository, times(1)).save(post);
        verify(kafkaSenderService, times(1)).send(postDtoWithPicture, user.getId());
    }

    @Test
    void createPostWithOutMultipartFile() throws IOException {
        PostDto postDto = PostDto.builder()
                .description("New description")
                .message("New message")
                .build();

        Post post = Post.builder()
                .id(1L)
                .description("New description")
                .message("New message")
                .createdAt(LocalDateTime.now())
                .build();

        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .build();

        when(postMapper.toEntity(postDto)).thenReturn(post);

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));

        Assertions.assertNull(multipartFile.getBytes());

        post.setImageName(null);
        post.setImageLink(null);
        post.setUser(user);

        PostDto postDtoWithPicture = PostDto.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName(null)
                .imageLink(null)
                .build();

        when(postMapper.toDto(post)).thenReturn(postDtoWithPicture);

        Assertions.assertEquals(postDtoWithPicture, postService.createPost(postDto, null));
        verify(postRepository, times(1)).save(post);
        verify(kafkaSenderService, times(1)).send(postDtoWithPicture, user.getId());
    }

    @Test
    void createPostWithMultipartFileNotCreateBucket() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        PostDto postDto = PostDto.builder()
                .description("New description")
                .message("New message")
                .build();

        Post post = Post.builder()
                .id(1L)
                .description("New description")
                .message("New message")
                .createdAt(LocalDateTime.now())
                .build();

        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .build();

        when(multipartFile.getInputStream()).thenReturn(new FileInputStream("src/test/resources/picture_for_test.jpg"));

        when(postMapper.toEntity(postDto)).thenReturn(post);

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));

        Assertions.assertFalse(multipartFile.isEmpty());

        when(minioProperties.getBucket()).thenReturn("images");
        when(minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build())).thenReturn(false);

        minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());

        when(multipartFile.getContentType()).thenReturn(Files.probeContentType(Path.of("src/test/resources/picture_for_test.jpg")));

        when(multipartFile.getOriginalFilename()).thenReturn("UUID + Date + picture_for_test.jpg");

        FileInputStream fileInputStream = new FileInputStream("src/test/resources/picture_for_test.jpg");

        when(minioClient.putObject(PutObjectArgs.builder().stream(fileInputStream, fileInputStream.available(), -1)
                .bucket(minioProperties.getBucket()).object("UUID + Date + picture_for_test.jpg").build()))
                .thenReturn(new ObjectWriteResponse(null, "images", null, "UUID + Date + picture_for_test.jpg", null, null));

        when(minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(minioProperties.getBucket())
                .object("UUID + Date + picture_for_test.jpg")
                .build())).thenReturn("http://localhost:9000/images/UUID_Date_picture_for_test.jpg");

        post.setImageName("UUID + Date + picture_for_test.jpg");
        post.setImageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg");
        post.setUser(user);

        PostDto postDtoWithPicture = PostDto.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName("UUID + Date + picture_for_test.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg")
                .build();

        when(postMapper.toDto(post)).thenReturn(postDtoWithPicture);

        Assertions.assertEquals(postDtoWithPicture, postService.createPost(postDto, multipartFile));
        verify(postRepository, times(1)).save(post);
        verify(kafkaSenderService, times(1)).send(postDtoWithPicture, user.getId());    }

    @Test
    void createPostWithMultipartFileAlreadyCreateBucket() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        PostDto postDto = PostDto.builder()
                .description("New description")
                .message("New message")
                .build();

        Post post = Post.builder()
                .id(1L)
                .description("New description")
                .message("New message")
                .createdAt(LocalDateTime.now())
                .build();

        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .build();

        PostDto postDtoWithPicture = PostDto.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName("UUID + Date + picture_for_test.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg")
                .build();

        when(postMapper.toEntity(postDto)).thenReturn(post);

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));

        Assertions.assertFalse(multipartFile.isEmpty());

        when(minioProperties.getBucket()).thenReturn("images");

        doThrow(BadInputDataException.class).when(minioClient).bucketExists(any());

        Assertions.assertThrows(BadInputDataException.class, () -> postService.createPost(postDto, multipartFile));
        verify(postRepository, never()).save(post);
        verify(kafkaSenderService, never()).send(postDtoWithPicture, user.getId());
    }

    @Test
    void createPostWithMultipartFileNotValid() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        PostDto postDto = PostDto.builder()
                .description("New description")
                .message("New message")
                .build();

        Post post = Post.builder()
                .id(1L)
                .description("New description")
                .message("New message")
                .createdAt(LocalDateTime.now())
                .build();

        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .build();

        when(postMapper.toEntity(postDto)).thenReturn(post);

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));

        Assertions.assertFalse(multipartFile.isEmpty());

        when(minioProperties.getBucket()).thenReturn("images");

        when(minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build())).thenReturn(true);

        when(multipartFile.getContentType()).thenReturn(Files.probeContentType(Path.of("src/test/resources/text.txt")));

        PostDto postDtoWithPicture = PostDto.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName("UUID + Date + picture_for_test.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg")
                .build();

        Assertions.assertThrows(BadInputDataException.class, () -> postService.createPost(postDto, multipartFile));
        verify(postRepository, never()).save(post);
        verify(kafkaSenderService, never()).send(postDtoWithPicture, user.getId());
    }

    @Test
    void createPostWithMultipartFileNotSaveImage() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        PostDto postDto = PostDto.builder()
                .description("New description")
                .message("New message")
                .build();

        Post post = Post.builder()
                .id(1L)
                .description("New description")
                .message("New message")
                .createdAt(LocalDateTime.now())
                .build();

        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .build();

        when(multipartFile.getInputStream()).thenReturn(new FileInputStream("src/test/resources/picture_for_test.jpg"));

        when(postMapper.toEntity(postDto)).thenReturn(post);

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user.getName())).thenReturn(Optional.of(user));

        Assertions.assertFalse(multipartFile.isEmpty());

        when(minioProperties.getBucket()).thenReturn("images");
        when(minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build())).thenReturn(true);

        when(multipartFile.getContentType()).thenReturn(Files.probeContentType(Path.of("src/test/resources/picture_for_test.jpg")));

        when(multipartFile.getOriginalFilename()).thenReturn("UUID + Date + picture_for_test.jpg");

        doThrow(BadInputDataException.class).when(minioClient).putObject(any());

        PostDto postDtoWithPicture = PostDto.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName("UUID + Date + picture_for_test.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg")
                .build();

        Assertions.assertThrows(BadInputDataException.class, () -> postService.createPost(postDto, multipartFile));
        verify(postRepository, never()).save(post);
        verify(kafkaSenderService, never()).send(postDtoWithPicture, user.getId());
    }

    @Test
    void getPost() {
        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .posts(new ArrayList<>())
                .build();

        Post post = Post.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName("UUID + Date + picture_for_test.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg")
                .user(user)
                .build();

        user.setPosts(List.of(post));

        PostDto postDto = PostDto.builder()
                .id(1L)
                .createdAt(post.getCreatedAt())
                .description(post.getDescription())
                .message(post.getMessage())
                .imageName(post.getImageName())
                .imageLink(post.getImageLink())
                .authorName(post.getUser().getName())
                .build();

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        when(postMapper.toDto(post)).thenReturn(postDto);

        Assertions.assertEquals(postDto, postService.getPost(post.getId()));
    }

    @Test
    void getNotExistPost() {
        doThrow(PostNotFoundException.class).when(postRepository).findById(anyLong());
        Assertions.assertThrows(PostNotFoundException.class, () -> postService.getPost(anyLong()));
    }

    @Test
    void updatePost() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .posts(new ArrayList<>())
                .build();

        Post post = Post.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName("UUID + Date + picture.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture.jpg")
                .user(user)
                .build();

        user.setPosts(List.of(post));

        PostDto updatePostDto = PostDto.builder()
                .description("Update description")
                .message("Update message")
                .build();

        Post updatePost = Post.builder()
                .description("Update description")
                .message("Update message")
                .createdAt(LocalDateTime.now())
                .build();

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        Assertions.assertTrue(post.getUser().getName().equals(user.getName()));

        when(postMapper.toEntity(updatePostDto)).thenReturn(updatePost);

        when(multipartFile.getInputStream()).thenReturn(new FileInputStream("src/test/resources/picture_for_test.jpg"));

        Assertions.assertFalse(multipartFile.isEmpty());

        when(minioProperties.getBucket()).thenReturn("images");
        when(minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build())).thenReturn(true);

        when(multipartFile.getContentType()).thenReturn(Files.probeContentType(Path.of("src/test/resources/picture_for_test.jpg")));

        when(multipartFile.getOriginalFilename()).thenReturn("UUID + Date + picture_for_test.jpg");

        FileInputStream fileInputStream = new FileInputStream("src/test/resources/picture_for_test.jpg");

        when(minioClient.putObject(PutObjectArgs.builder().stream(fileInputStream, fileInputStream.available(), -1)
                .bucket(minioProperties.getBucket()).object("UUID + Date + picture_for_test.jpg").build()))
                .thenReturn(new ObjectWriteResponse(null, "images", null, "UUID + Date + picture_for_test.jpg", null, null));

        when(minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(minioProperties.getBucket())
                .object("UUID + Date + picture_for_test.jpg")
                .build())).thenReturn("http://localhost:9000/images/UUID_Date_picture_for_test.jpg");

        updatePost.setImageName("UUID + Date + picture_for_test.jpg");
        updatePost.setImageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg");

        Assertions.assertNotNull(post.getImageName());

        updatePost.setId(post.getId());
        updatePost.setUser(post.getUser());

        PostDto updatePostDtoWithPicture = PostDto.builder()
                .id(1L)
                .createdAt(updatePost.getCreatedAt())
                .description(updatePost.getDescription())
                .message(updatePost.getMessage())
                .imageName("UUID + Date + picture_for_test.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg")
                .build();

        when(postMapper.toDto(updatePost)).thenReturn(updatePostDtoWithPicture);

        Assertions.assertEquals(updatePostDtoWithPicture, postService.updatePost(post.getId(), updatePostDto, multipartFile));
        verify(minioClient, times(1)).removeObject(RemoveObjectArgs.builder().bucket(minioProperties.getBucket()).object("UUID + Date + picture.jpg").build());
        verify(postRepository, times(1)).save(updatePost);
        verify(kafkaSenderService, times(1)).send(updatePostDtoWithPicture, user.getId());
    }

    @Test
    void updatePostAccessDenied() {
        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .posts(new ArrayList<>())
                .build();

        Post post = Post.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName("UUID + Date + picture.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture.jpg")
                .user(user2)
                .build();

        user2.setPosts(List.of(post));

        PostDto updatePostDto = PostDto.builder()
                .description("Update description")
                .message("Update message")
                .build();

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        Assertions.assertFalse(user.getName().equals(post.getUser().getName()));

        Assertions.assertThrows(AccessDeniedException.class, () -> postService.updatePost(post.getId(), updatePostDto, multipartFile));
    }

    @Test
    void updatePostWhenInitiallyWasNoImage() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .posts(new ArrayList<>())
                .build();

        Post post = Post.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName(null)
                .imageLink(null)
                .user(user)
                .build();

        user.setPosts(List.of(post));

        PostDto updatePostDto = PostDto.builder()
                .description("Update description")
                .message("Update message")
                .build();

        Post updatePost = Post.builder()
                .description("Update description")
                .message("Update message")
                .createdAt(LocalDateTime.now())
                .build();

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        Assertions.assertTrue(post.getUser().getName().equals(user.getName()));

        when(postMapper.toEntity(updatePostDto)).thenReturn(updatePost);

        when(multipartFile.getInputStream()).thenReturn(new FileInputStream("src/test/resources/picture_for_test.jpg"));

        Assertions.assertFalse(multipartFile.isEmpty());

        when(minioProperties.getBucket()).thenReturn("images");
        when(minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build())).thenReturn(true);

        when(multipartFile.getContentType()).thenReturn(Files.probeContentType(Path.of("src/test/resources/picture_for_test.jpg")));

        when(multipartFile.getOriginalFilename()).thenReturn("UUID + Date + picture_for_test.jpg");

        FileInputStream fileInputStream = new FileInputStream("src/test/resources/picture_for_test.jpg");

        when(minioClient.putObject(PutObjectArgs.builder().stream(fileInputStream, fileInputStream.available(), -1)
                .bucket(minioProperties.getBucket()).object("UUID + Date + picture_for_test.jpg").build()))
                .thenReturn(new ObjectWriteResponse(null, "images", null, "UUID + Date + picture_for_test.jpg", null, null));

        when(minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(minioProperties.getBucket())
                .object("UUID + Date + picture_for_test.jpg")
                .build())).thenReturn("http://localhost:9000/images/UUID_Date_picture_for_test.jpg");

        updatePost.setImageName("UUID + Date + picture_for_test.jpg");
        updatePost.setImageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg");

        updatePost.setId(post.getId());
        updatePost.setUser(post.getUser());

        PostDto updatePostDtoWithPicture = PostDto.builder()
                .id(1L)
                .createdAt(updatePost.getCreatedAt())
                .description(updatePost.getDescription())
                .message(updatePost.getMessage())
                .imageName("UUID + Date + picture_for_test.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture_for_test.jpg")
                .build();

        when(postMapper.toDto(updatePost)).thenReturn(updatePostDtoWithPicture);

        Assertions.assertEquals(updatePostDtoWithPicture, postService.updatePost(post.getId(), updatePostDto, multipartFile));
        verify(minioClient, never()).removeObject(RemoveObjectArgs.builder().bucket(minioProperties.getBucket()).object("UUID + Date + picture.jpg").build());
        verify(postRepository, times(1)).save(updatePost);
        verify(kafkaSenderService, times(1)).send(updatePostDtoWithPicture, user.getId());
    }

    @Test
    void deletePostWithImage() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .posts(new ArrayList<>())
                .build();

        Post post = Post.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName("UUID + Date + picture.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture.jpg")
                .user(user)
                .build();

        user.setPosts(List.of(post));

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        Assertions.assertTrue(post.getUser().getName().equals(user.getName()));

        when(minioProperties.getBucket()).thenReturn("images");

        Assertions.assertEquals(post.getId(), postService.deletePost(post.getId()));
        verify(minioClient, times(1)).removeObject(RemoveObjectArgs.builder().bucket(minioProperties.getBucket()).object("UUID + Date + picture.jpg").build());
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void deletePostWithoutImage() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .friends(Collections.emptySet())
                .followers(Collections.emptySet())
                .posts(new ArrayList<>())
                .build();

        Post post = Post.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName(null)
                .imageLink(null)
                .user(user)
                .build();

        user.setPosts(List.of(post));

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        doReturn(user.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        Assertions.assertTrue(post.getUser().getName().equals(user.getName()));

        when(minioProperties.getBucket()).thenReturn("images");

        Assertions.assertEquals(post.getId(), postService.deletePost(post.getId()));
        verify(minioClient, never()).removeObject(RemoveObjectArgs.builder().bucket(minioProperties.getBucket()).object("UUID + Date + picture.jpg").build());
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void getFeedUser() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .followers(new HashSet<>())
                .posts(new ArrayList<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .followers(new HashSet<>())
                .posts(new ArrayList<>())
                .build();

        User user3 = User.builder()
                .id(3L)
                .name("Alina")
                .followers(new HashSet<>())
                .posts(new ArrayList<>())
                .build();

        Post post1 = Post.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("Description 1")
                .message("Message 1")
                .imageName("UUID + Date + picture1.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture1.jpg")
                .user(user2)
                .build();

        Post post2 = Post.builder()
                .id(2L)
                .createdAt(LocalDateTime.now().plusSeconds(30))
                .description("Description 1")
                .message("Message 2")
                .imageName("UUID + Date + picture2.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture2.jpg")
                .user(user3)
                .build();

        user2.setPosts(List.of(post1));
        user3.setPosts(List.of(post2));
        user1.addFollower(user2);
        user1.addFollower(user3);

        PostDto postDto1 = PostDto.builder()
                .id(1L)
                .createdAt(post1.getCreatedAt())
                .description(post1.getDescription())
                .message(post1.getMessage())
                .imageName(post1.getImageName())
                .imageLink(post1.getImageLink())
                .authorName(post1.getUser().getName())
                .build();

        PostDto postDto2 = PostDto.builder()
                .id(2L)
                .createdAt(post2.getCreatedAt())
                .description(post2.getDescription())
                .message(post2.getMessage())
                .imageName(post2.getImageName())
                .imageLink(post2.getImageLink())
                .authorName(post2.getUser().getName())
                .build();

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));

        when(pageRequest.getPageNumber()).thenReturn(0);
        when(pageRequest.getPageSize()).thenReturn(2);
        when(pageRequest.getSort()).thenReturn(Sort.by("created_at"));

        Page<Post> lastPostsFollowers = new PageImpl<>(List.of(post1, post2));

        when(postRepository.getLastPostsFollowers(user1.getId(), pageRequest)).thenReturn(lastPostsFollowers);

        List<Post> content = lastPostsFollowers.getContent();

        when(postMapper.toListDto(content)).thenReturn(List.of(postDto1, postDto2));

        Assertions.assertEquals(List.of(postDto1, postDto2), postService.getFeedUser(pageRequest));
        Assertions.assertNotEquals(List.of(postDto2, postDto1), postService.getFeedUser(pageRequest));
        Assertions.assertNotEquals(List.of(postDto1), postService.getFeedUser(pageRequest));
        Assertions.assertNotEquals(List.of(postDto2), postService.getFeedUser(pageRequest));
    }

    @Test
    void getFeedUserWhenNotFollowers() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .followers(new HashSet<>())
                .posts(new ArrayList<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .followers(new HashSet<>())
                .posts(new ArrayList<>())
                .build();

        User user3 = User.builder()
                .id(3L)
                .name("Alina")
                .followers(new HashSet<>())
                .posts(new ArrayList<>())
                .build();

        Post post1 = Post.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("Description 1")
                .message("Message 1")
                .imageName("UUID + Date + picture1.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture1.jpg")
                .user(user2)
                .build();

        Post post2 = Post.builder()
                .id(2L)
                .createdAt(LocalDateTime.now().plusSeconds(30))
                .description("Description 1")
                .message("Message 2")
                .imageName("UUID + Date + picture2.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture2.jpg")
                .user(user3)
                .build();

        user2.setPosts(List.of(post1));
        user3.setPosts(List.of(post2));

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByName(user1.getName())).thenReturn(Optional.of(user1));

        when(pageRequest.getPageNumber()).thenReturn(0);
        when(pageRequest.getPageSize()).thenReturn(2);
        when(pageRequest.getSort()).thenReturn(Sort.by("created_at"));

        Page<Post> lastPostsFollowers = new PageImpl<>(Collections.emptyList());

        when(postRepository.getLastPostsFollowers(user1.getId(), pageRequest)).thenReturn(lastPostsFollowers);

        Assertions.assertEquals(Collections.emptyList(), postService.getFeedUser(pageRequest));
    }

    @Test
    void getAllPosts() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .role(Role.ROLE_USER)
                .build();

        Post post = Post.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName("UUID + Date + picture.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture.jpg")
                .user(user1)
                .build();

        user1.setPosts(List.of(post));

        User user2 = User.builder()
                .id(2L)
                .name("Tom")
                .role(Role.ROLE_ADMIN)
                .build();

        PostDto postDto = PostDto.builder()
                .id(1L)
                .createdAt(post.getCreatedAt())
                .description(post.getDescription())
                .message(post.getMessage())
                .imageName(post.getImageName())
                .imageLink(post.getImageLink())
                .build();

        doReturn(user2.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.getAllNameAdmins()).thenReturn(List.of(user2.getName()));
        when(postRepository.findAll()).thenReturn(List.of(post));
        when(postMapper.toListDto(List.of(post))).thenReturn(List.of(postDto));

        Assertions.assertEquals(List.of(postDto), postService.getAllPosts());
    }

    @Test
    void getAllPostsAccessDenied() {
        User user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .role(Role.ROLE_USER)
                .build();

        Post post = Post.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("New description")
                .message("New message")
                .imageName("UUID + Date + picture.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture.jpg")
                .user(user1)
                .build();

        user1.setPosts(List.of(post));

        User user2 = User.builder()
                .id(2L)
                .name("Tom")
                .role(Role.ROLE_ADMIN)
                .build();

        doReturn(user1.getName()).when(authentication).getName();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.getAllNameAdmins()).thenReturn(List.of(user2.getName()));

        Assertions.assertThrows(AccessDeniedException.class, () -> postService.getAllPosts());
    }

}