package com.utochkin.kafkaproducerforsma.services.impl;

import com.utochkin.kafkaproducerforsma.dto.ChatDto;
import com.utochkin.kafkaproducerforsma.exceptions.BadInputDataException;
import com.utochkin.kafkaproducerforsma.exceptions.ChatNotFoundException;
import com.utochkin.kafkaproducerforsma.mappers.ChatMapper;
import com.utochkin.kafkaproducerforsma.models.Chat;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.repository.ChatRepository;
import com.utochkin.kafkaproducerforsma.repository.MessageRepository;
import com.utochkin.kafkaproducerforsma.services.interfaces.ChatService;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ChatMapper chatMapper;
    private final UserService userService;

    @Transactional(readOnly = true)
    @Cacheable(value = "ChatService::getChatById", key = "#chatId")
    @Override
    public ChatDto getChatById(Long chatId) {
        Chat chatById = chatRepository.findById(chatId).orElseThrow(ChatNotFoundException::new);
        ChatDto chatDto = chatMapper.toDto(chatById);

        chatDto.setLastMessage(getLastMessage(chatId));

        List<Long> collect = chatById.getUsers().stream().map(User::getId).takeWhile(Objects::nonNull).toList();
        if (collect.size() > 1) {
            chatDto.setFirstUserId(collect.get(0));
            chatDto.setSecondUserId(collect.get(1));
        } else {
            chatDto.setFirstUserId(collect.get(0));
            chatDto.setSecondUserId(0L);
        }
        return chatDto;
    }

    @Override
    public ChatDto createChat(ChatDto chatDto) {

        var firstUser = userService.getById(chatDto.getFirstUserId());
        var secondUser = userService.getById(chatDto.getSecondUserId());

        if (!firstUser.getFriends().contains(secondUser)) {
            throw new BadInputDataException("These users can't have a chat");
        }

        if (firstUser.getChats().stream()
                .anyMatch(chat -> secondUser.getChats().contains(chat))) {
            throw new BadInputDataException("These users already have a chat");
        }

        Chat chat = chatMapper.toChat(chatDto);

        Set<User> users = new HashSet<>() {
            {
                add(firstUser);
                add(secondUser);
            }
        };

        chat.setUsers(users);
        chat.setCreatedAt(LocalDateTime.now());

        chatRepository.save(chat);
        return chatMapper.toDto(chat);
    }

    @CacheEvict(value = "ChatService::getChatById", key = "#chatId")
    @Override
    public void deleteChatById(Long chatId) {
        chatRepository.findById(chatId).orElseThrow(ChatNotFoundException::new);
        chatRepository.deleteById(chatId);
    }


    @Override
    public void joinChat(Long userId, Long chatId) {
        var user = userService.getById(userId);
        var chatDto = getChatById(chatId);
        Chat chat = chatMapper.toChat(chatDto);

        Optional<User> oneManChat = chat.getUsers().stream().findFirst();

        if (oneManChat.isPresent() && !oneManChat.get().getFriends().contains(user)) {
            throw new BadInputDataException("This user can't join in this chat");
        }

        chat.getUsers().add(user);

        chatRepository.save(chat);
    }

    @Override
    public void leaveChat(Long userId, Long chatId) {

        var user = userService.getById(userId);
        var chatDto = getChatById(chatId);
        Chat chat = chatMapper.toChat(chatDto);

        chat.getUsers().remove(user);

        chatRepository.save(chat);
    }

    @Transactional(readOnly = true)
    @Override
    public String getLastMessage(Long chatId) {
        chatRepository.findById(chatId).orElseThrow(ChatNotFoundException::new);
        return messageRepository.getLastMessageFromChat(chatId);
    }

}
