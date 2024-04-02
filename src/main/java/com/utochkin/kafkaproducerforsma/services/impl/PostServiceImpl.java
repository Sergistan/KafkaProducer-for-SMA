package com.utochkin.kafkaproducerforsma.services.impl;

import com.utochkin.kafkaproducerforsma.dto.PostDto;
import com.utochkin.kafkaproducerforsma.exceptions.AccessDeniedException;
import com.utochkin.kafkaproducerforsma.exceptions.BadInputDataException;
import com.utochkin.kafkaproducerforsma.exceptions.PostNotFoundException;
import com.utochkin.kafkaproducerforsma.exceptions.UserNotFoundException;
import com.utochkin.kafkaproducerforsma.mappers.PostMapper;
import com.utochkin.kafkaproducerforsma.models.Post;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.props.MinioProperties;
import com.utochkin.kafkaproducerforsma.repository.PostRepository;
import com.utochkin.kafkaproducerforsma.repository.UserRepository;
import com.utochkin.kafkaproducerforsma.sender.KafkaSenderService;
import com.utochkin.kafkaproducerforsma.services.interfaces.PostService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final KafkaSenderService kafkaSenderService;

    @Override
    public PostDto createPost(PostDto postDto, MultipartFile file) {
        Post createdPost = postMapper.toEntity(postDto);
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByName(name).orElseThrow(UserNotFoundException::new);

        if (file == null) {
            createdPost.setImageName(null);
            createdPost.setImageLink(null);
        } else {
            String fileName = checkAndSaveImageAtMinioAndGetFilename(file);
            createdPost.setImageName(fileName);
            createdPost.setImageLink(getUrlImage(fileName));
        }

        createdPost.setUser(user);
        postRepository.save(createdPost);
        PostDto savedPostDto = postMapper.toDto(createdPost);

        kafkaSenderService.send(savedPostDto, user.getId());

        return savedPostDto;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "PostService::getPost", key = "#postId")
    @Override
    public PostDto getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        return postMapper.toDto(post);
    }

    @CachePut(value = "PostService::getPost", key = "#postId")
    @Override
    public PostDto updatePost(Long postId, PostDto postDto, MultipartFile file) {
        Post post = checkAccessReturnPostId(postId);
        Post updatePost = postMapper.toEntity(postDto);

        if (file == null) {
            updatePost.setImageName(null);
            updatePost.setImageLink(null);
        } else {
            String fileName = checkAndSaveImageAtMinioAndGetFilename(file);
            updatePost.setImageLink(getUrlImage(fileName));
            updatePost.setImageName(fileName);
        }

        if (post.getImageName() != null) {
            deleteImage(post.getImageName());
        }
        updatePost.setId(postId);
        updatePost.setUser(post.getUser());
        postRepository.save(updatePost);
        PostDto updatedPostDto = postMapper.toDto(updatePost);

        kafkaSenderService.send(updatedPostDto, post.getUser().getId());

        return updatedPostDto;
    }

    @CacheEvict(value = "PostService::getPost", key = "#postId")
    @Override
    public Long deletePost(Long postId) {
        Post post = checkAccessReturnPostId(postId);
        if (post.getImageName() != null) {
            deleteImage(post.getImageName());
        }
        postRepository.delete(post);
        return post.getId();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PostDto> getFeedUser(Pageable pageable) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByName(name).orElseThrow(UserNotFoundException::new);
        Page<Post> lastPostsFollowers = postRepository.getLastPostsFollowers(user.getId(), pageable);
        List<Post> content = lastPostsFollowers.getContent();
        return postMapper.toListDto(content);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PostDto> getAllPosts() {
        checkAccessByAdmin();
        return postMapper.toListDto(postRepository.findAll());
    }

    public Post checkAccessReturnPostId(Long postId) throws AccessDeniedException, PostNotFoundException {
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!name.equals(post.getUser().getName())) {
            throw new AccessDeniedException("Error: access denied!");
        } else return post;
    }

    public void checkAccessByAdmin() throws AccessDeniedException {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        List<String> allNameAdmins = userRepository.getAllNameAdmins();
        if (allNameAdmins.stream().noneMatch(x -> x.equals(name))) {
            throw new AccessDeniedException("Error: access denied!");
        }
    }

    @NotNull
    private String checkAndSaveImageAtMinioAndGetFilename(MultipartFile file) {
        try {
            createBucket();
        } catch (Exception e) {
            throw new BadInputDataException("Image upload failed: " + e.getMessage());
        }
        if (!isValid(file)) {
            throw new BadInputDataException("Incorrect input file");
        }
        String fileName = generateFileName(file);
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
            saveImage(inputStream, fileName);
            inputStream.close();
        } catch (Exception e) {
            throw new BadInputDataException("Image upload failed: " + e.getMessage());
        }
        return fileName;
    }

    private boolean isValid(MultipartFile multipartFile) {
        boolean result = true;
        String contentType = multipartFile.getContentType();
        Objects.requireNonNull(contentType, "Content-Type must not be null.");
        if (!isSupportedContentType(contentType)) {
            result = false;
        }
        return result;
    }

    private boolean isSupportedContentType(String contentType) {
        return contentType.equals("image/png")
                || contentType.equals("image/jpg")
                || contentType.equals("image/jpeg");
    }

    @SneakyThrows
    private void createBucket() {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.getBucket())
                .build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build());
        } else {
            System.out.println("Bucket " + minioProperties.getBucket() + " already exists.");
        }
    }

    private String generateFileName(final MultipartFile file) {
        String name = file.getOriginalFilename();
        return UUID.randomUUID() + ": <" + LocalDateTime.now() + "> " + name;
    }

    @SneakyThrows
    private void saveImage(final InputStream inputStream,
                           final String fileName) {
        minioClient.putObject(PutObjectArgs.builder()
                .stream(inputStream, inputStream.available(), -1)
                .bucket(minioProperties.getBucket())
                .object(fileName)
                .build());
    }

    @SneakyThrows
    private String getUrlImage(String fileName) {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(minioProperties.getBucket())
                        .object(fileName)
                        .build());
    }

    @SneakyThrows
    private void deleteImage(String fileName) {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .object(fileName)
                .build());
    }
}
