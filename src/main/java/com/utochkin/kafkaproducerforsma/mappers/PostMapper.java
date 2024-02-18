package com.utochkin.kafkaproducerforsma.mappers;


import com.utochkin.kafkaproducerforsma.dto.PostDto;
import com.utochkin.kafkaproducerforsma.models.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {

    PostDto toDto (Post post);
    List<PostDto> toListDto (List<Post> posts);

    @Mapping(target = "id" , ignore = true)
    @Mapping(target = "user" , ignore = true)
    Post toEntity (PostDto postDto);

}
