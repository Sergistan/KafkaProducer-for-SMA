package com.utochkin.kafkaproducerforsma.repository;


import com.utochkin.kafkaproducerforsma.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT p.id, p.description, p.message, p.image_link, p.image_name, p.created_at, p.user_id FROM posts p JOIN followers f ON p.user_id = f.user_id  JOIN users u ON u.id = f.follower_id WHERE u.id = ?1", nativeQuery = true)
    Page<Post> getLastPostsFollowers(Long userId, Pageable pageable);
}
