package com.utochkin.kafkaproducerforsma.repository;

import com.utochkin.kafkaproducerforsma.models.Post;
import com.utochkin.kafkaproducerforsma.models.Role;
import com.utochkin.kafkaproducerforsma.models.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private User user1;
    private User user2;
    private User user3;
    private Post post1;
    private Post post2;
    private Post savePost1;
    private Post savePost2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .followers(new HashSet<>())
                .posts(new ArrayList<>())
                .build();

        user2 = User.builder()
                .id(2L)
                .name("Ilya")
                .password(passwordEncoder.encode("222"))
                .email("dzaga73i98@gmail.com")
                .role(Role.ROLE_USER)
                .followers(new HashSet<>())
                .posts(new ArrayList<>())
                .build();

        user3 = User.builder()
                .id(3L)
                .name("Alina")
                .password(passwordEncoder.encode("333"))
                .email("umamimi@ya.com")
                .role(Role.ROLE_USER)
                .followers(new HashSet<>())
                .posts(new ArrayList<>())
                .build();

        post1 = Post.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .description("Description 1")
                .message("Message 1")
                .imageName("UUID + Date + picture1.jpg")
                .imageLink("http://localhost:9000/images/UUID_Date_picture1.jpg")
                .user(user2)
                .build();

        post2 = Post.builder()
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
        user2.addFollower(user1);
        user3.addFollower(user1);

        userRepository.save(user1);
        savePost1 = postRepository.save(post1);
        userRepository.save(user2);
        savePost2 = postRepository.save(post2);
        userRepository.save(user3);
    }

    @Test
    void getLastPostsFollowersASC() {
        Pageable paging = PageRequest.of(0, 2, Sort.by("created_at"));

        Page<Post> lastPostsFollowers = postRepository.getLastPostsFollowers(user1.getId(), paging);
        List<Post> content = lastPostsFollowers.getContent();

        Assertions.assertEquals(2, lastPostsFollowers.getTotalElements());
        Assertions.assertEquals(content.get(0), savePost1);
        Assertions.assertEquals(content.get(1), savePost2);
    }


    @Test
    void getLastPostsFollowersDESC() {
        Pageable paging = PageRequest.of(0, 2, Sort.by("created_at").descending());

        Page<Post> lastPostsFollowers = postRepository.getLastPostsFollowers(user1.getId(), paging);
        List<Post> content = lastPostsFollowers.getContent();

        Assertions.assertEquals(2, lastPostsFollowers.getTotalElements());
        Assertions.assertEquals(content.get(0), savePost2);
        Assertions.assertEquals(content.get(1), savePost1);
    }

    @Test
    void getLastPostsFollowersPageWhereThereAreNoPosts() {
        Pageable paging = PageRequest.of(1, 2, Sort.by("created_at"));

        Page<Post> lastPostsFollowers = postRepository.getLastPostsFollowers(user1.getId(), paging);
        List<Post> content = lastPostsFollowers.getContent();

        Assertions.assertEquals(2, lastPostsFollowers.getTotalElements());
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> content.get(0));
    }

    @Test
    void getLastPostsFollowersPageWhenPageSizeEquals1() {
        Pageable paging = PageRequest.of(0, 1, Sort.by("created_at"));

        Page<Post> lastPostsFollowers = postRepository.getLastPostsFollowers(user1.getId(), paging);
        List<Post> content = lastPostsFollowers.getContent();

        Assertions.assertEquals(2, lastPostsFollowers.getTotalElements());
        Assertions.assertEquals(content.get(0), savePost1);
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> content.get(1));
    }
}