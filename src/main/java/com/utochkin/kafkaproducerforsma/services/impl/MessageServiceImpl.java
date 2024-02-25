package com.utochkin.kafkaproducerforsma.services.impl;


import com.utochkin.kafkaproducerforsma.dto.ChatDto;
import com.utochkin.kafkaproducerforsma.dto.MessageDto;
import com.utochkin.kafkaproducerforsma.dto.UpdateMessageDto;
import com.utochkin.kafkaproducerforsma.dto.request.MessageDeleteIdRequest;
import com.utochkin.kafkaproducerforsma.exceptions.BadInputDataException;
import com.utochkin.kafkaproducerforsma.exceptions.ChatNotFoundException;
import com.utochkin.kafkaproducerforsma.exceptions.MessageNotFoundException;
import com.utochkin.kafkaproducerforsma.mappers.ChatMapper;
import com.utochkin.kafkaproducerforsma.mappers.MessageMapper;
import com.utochkin.kafkaproducerforsma.models.Chat;
import com.utochkin.kafkaproducerforsma.models.Message;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.repository.ChatRepository;
import com.utochkin.kafkaproducerforsma.repository.MessageRepository;
import com.utochkin.kafkaproducerforsma.services.interfaces.ChatService;
import com.utochkin.kafkaproducerforsma.services.interfaces.MessageService;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ChatService chatService;
    private final MessageMapper messageMapper;
    private final ChatMapper chatMapper;
    private final ChatRepository chatRepository;

    @Override
    public MessageDto addMessage(MessageDto messageDto) {

        User user = userService.findByName(messageDto.getSenderName());
        ChatDto chatDto = chatService.getChatByIdFromMessageDto(messageDto.getChatId());
        if (!chatDto.getUsers().contains(user)){
            throw new BadInputDataException("Your name is incorrect!");
        }

        Chat chat = chatMapper.toChat(chatDto);
        Message message = messageMapper.toMessage(messageDto, user, chat);

        messageRepository.save(message);
        setAndSaveLastMessageAtChatById(messageDto.getChatId());

        return messageMapper.toMessageDto(message);
    }

    @Override
    public MessageDeleteIdRequest deleteById(MessageDeleteIdRequest messageDeleteIdRequest) {
        messageRepository.deleteById(messageDeleteIdRequest.getIdMessage());

        setAndSaveLastMessageAtChatById(messageDeleteIdRequest.getIdChat());

        log.info("Message with id = {} deleted.", messageDeleteIdRequest.getIdMessage());
        return new MessageDeleteIdRequest(messageDeleteIdRequest.getIdMessage(), messageDeleteIdRequest.getIdChat(), true);
    }

    @Override
    public UpdateMessageDto update(UpdateMessageDto updateMessageDto) {
        User user = userService.findByName(updateMessageDto.getSenderName());
        ChatDto chatDto = chatService.getChatByIdFromMessageDto(updateMessageDto.getChatId());
        if (!chatDto.getUsers().contains(user)){
            throw new BadInputDataException("Your name is incorrect!");
        }

        Message message = messageRepository.findById(updateMessageDto.getId()).orElseThrow(MessageNotFoundException::new);
        Message updateMessage = messageMapper.update(message, updateMessageDto);
        messageRepository.save(updateMessage);

        setAndSaveLastMessageAtChatById(updateMessageDto.getChatId());

        log.info("Message with id = {} updated.", updateMessage.getId());
        UpdateMessageDto updateMessageDtoAfterSave = messageMapper.toUpdateMessageDto(updateMessage);
        updateMessageDtoAfterSave.setUpdated(true);
        updateMessageDtoAfterSave.setSenderName(updateMessageDto.getSenderName());
        return updateMessageDtoAfterSave;
    }

    void setAndSaveLastMessageAtChatById(Long chatId){
        Chat chat = chatRepository.findById(chatId).orElseThrow(ChatNotFoundException::new);
        chat.setLastMessage(messageRepository.getLastMessageFromChat(chatId));
        chatRepository.save(chat);
    }
}
