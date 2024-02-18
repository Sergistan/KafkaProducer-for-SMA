package com.utochkin.kafkaproducerforsma.mappers;

import com.utochkin.kafkaproducerforsma.dto.UserDto;
import com.utochkin.kafkaproducerforsma.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto (User user);

    @Mapping(target = "id" , ignore = true)
    @Mapping(target = "role" , ignore = true)
    @Mapping(target = "posts" , ignore = true)
    @Mapping(target = "friends" , ignore = true)
    @Mapping(target = "followers" , ignore = true)
    @Mapping(target = "chats" , ignore = true)
    @Mapping(target = "messages" , ignore = true)
    User toEntity (UserDto userDto);
}
