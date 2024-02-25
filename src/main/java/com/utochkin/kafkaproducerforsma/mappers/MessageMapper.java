package com.utochkin.kafkaproducerforsma.mappers;


import com.utochkin.kafkaproducerforsma.dto.ChatDto;
import com.utochkin.kafkaproducerforsma.dto.MessageDto;
import com.utochkin.kafkaproducerforsma.dto.UpdateMessageDto;
import com.utochkin.kafkaproducerforsma.models.Chat;
import com.utochkin.kafkaproducerforsma.models.Message;
import com.utochkin.kafkaproducerforsma.models.User;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        imports = {
                LocalDateTime.class
        })
public interface MessageMapper {

    @Mapping(target = "sentAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sender", expression = "java(sender)")
    @Mapping(target = "chat", expression = "java(chat)")
    Message toMessage(MessageDto dto, User sender, Chat chat);

    @Mapping(target = "text", expression = "java(dto.getUpdatedText())")
    @Mapping(target = "sentAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "chat", ignore = true)
    Message update(@MappingTarget Message message, UpdateMessageDto dto);

    @Mapping(target = "chatId" , expression = "java(message.getChat().getId())")
    @Mapping(target = "senderName" , expression = "java(message.getSender().getName())")
    @Mapping(target = "sentAt", expression = "java(LocalDateTime.now())")
    MessageDto toMessageDto(Message message);

    @Mapping(target = "updatedText", expression = "java(updateMessage.getText())")
    @Mapping(target = "chatId", ignore = true)
    @Mapping(target = "senderName", ignore = true)
    @Mapping(target = "updated", ignore = true)
    UpdateMessageDto toUpdateMessageDto(Message updateMessage);

    List<MessageDto> toListDto(List<Message> messages);
}
