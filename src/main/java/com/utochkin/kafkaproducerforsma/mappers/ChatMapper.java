package com.utochkin.kafkaproducerforsma.mappers;


import com.utochkin.kafkaproducerforsma.dto.ChatDto;
import com.utochkin.kafkaproducerforsma.models.Chat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    @Mapping(target = "messages" , ignore = true)
    Chat toChat(ChatDto chatDto);

    @Mapping(target = "firstUserId" , ignore = true)
    @Mapping(target = "secondUserId" , ignore = true)
    ChatDto toDto (Chat chat);
}
