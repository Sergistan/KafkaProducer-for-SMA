package com.utochkin.kafkaproducerforsma.mappers;

import com.utochkin.kafkaproducerforsma.dto.UserDto;
import com.utochkin.kafkaproducerforsma.models.Role;
import com.utochkin.kafkaproducerforsma.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring",
        imports = {
                Role.class
        })
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "role", expression = "java(Role.ROLE_USER)")
    @Mapping(target = "posts", ignore = true)
    @Mapping(target = "friends", ignore = true)
    @Mapping(target = "followers", ignore = true)
    @Mapping(target = "friendRequests", ignore = true)
    @Mapping(target = "chats", ignore = true)
    @Mapping(target = "messages", ignore = true)
    User toEntity(UserDto userDto);

    List<UserDto> toListDto(List<User> users);
}
