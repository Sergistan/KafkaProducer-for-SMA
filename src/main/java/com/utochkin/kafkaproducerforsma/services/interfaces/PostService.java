package com.utochkin.kafkaproducerforsma.services.interfaces;


import com.utochkin.kafkaproducerforsma.dto.PostDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    PostDto createPost(PostDto postDto, MultipartFile file);

    PostDto getPost(Long postId);

    PostDto updatePost(Long postId, PostDto postDto, MultipartFile file);

    Long deletePost(Long postId);

    List<PostDto> getFeedUser(Pageable paging);

    List <PostDto> getAllPosts ();
}
