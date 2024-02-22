package com.utochkin.kafkaproducerforsma.mappers;


import com.utochkin.kafkaproducerforsma.dto.PostDto;
import com.utochkin.kafkaproducerforsma.models.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", imports = {
        LocalDateTime.class
})
public interface PostMapper {
    @Mapping(target = "authorName", expression = "java(post.getUser().getName())")
    PostDto toDto(Post post);

    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "user", ignore = true)
    Post toEntity(PostDto postDto);

    List<PostDto> toListDto(List<Post> posts);

}
