package com.utochkin.kafkaproducerforsma.mappers;


import com.utochkin.kafkaproducerforsma.dto.ChatDto;
import com.utochkin.kafkaproducerforsma.models.Chat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    @Mapping(target = "messages", ignore = true)
    Chat toChat(ChatDto chatDto);

    @Mapping(target = "firstUserId", expression = "java(chatDto.getUsers().stream().map(User::getId).findFirst().get())")
    @Mapping(target = "secondUserId", expression = "java(chatDto.getUsers().stream().map(User::getId).skip(1).findFirst().orElse(null))")
    ChatDto toDto(Chat chat);

    List<ChatDto> toListDto(List<Chat> chats);
}
