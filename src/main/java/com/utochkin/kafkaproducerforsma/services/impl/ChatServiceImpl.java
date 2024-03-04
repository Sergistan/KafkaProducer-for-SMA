package com.utochkin.kafkaproducerforsma.services.impl;

import com.utochkin.kafkaproducerforsma.dto.ChatDto;
import com.utochkin.kafkaproducerforsma.dto.MessageDto;
import com.utochkin.kafkaproducerforsma.exceptions.AccessDeniedException;
import com.utochkin.kafkaproducerforsma.exceptions.BadInputDataException;
import com.utochkin.kafkaproducerforsma.exceptions.ChatNotFoundException;
import com.utochkin.kafkaproducerforsma.exceptions.UserNotFoundException;
import com.utochkin.kafkaproducerforsma.mappers.ChatMapper;
import com.utochkin.kafkaproducerforsma.mappers.MessageMapper;
import com.utochkin.kafkaproducerforsma.models.Chat;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.repository.ChatRepository;
import com.utochkin.kafkaproducerforsma.repository.MessageRepository;
import com.utochkin.kafkaproducerforsma.repository.UserRepository;
import com.utochkin.kafkaproducerforsma.services.interfaces.ChatService;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ChatMapper chatMapper;
    private final MessageMapper messageMapper;
    private final UserService userService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public ChatDto getChatById(Long chatId) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Chat chatById = chatRepository.findById(chatId).orElseThrow(ChatNotFoundException::new);

        if (chatById.getUsers().stream().map(User::getName).noneMatch(x -> x.equals(name))) {
            throw new AccessDeniedException("Error: access denied!");
        }

        ChatDto chatDto = chatMapper.toDto(chatById);

        chatDto.setLastMessage(getLastMessage(chatId));

        return chatDto;
    }

    @Override
    public ChatDto getChatByIdFromMessageDto(Long chatId) {
        Chat chatById = chatRepository.findById(chatId).orElseThrow(ChatNotFoundException::new);

        ChatDto chatDto = chatMapper.toDto(chatById);

        chatDto.setLastMessage(getLastMessageFromMessageDto(chatId));

        return chatDto;
    }

    @Override
    public ChatDto createChat(ChatDto chatDto) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        User firstUser = userService.getById(chatDto.getFirstUserId());
        User secondUser = userService.getById(chatDto.getSecondUserId());

        if ((!name.equals(firstUser.getName())) && (!(name.equals(secondUser.getName())))) {
            throw new AccessDeniedException("Error: access denied!");
        }

        if (!firstUser.getFriends().contains(secondUser)) {
            throw new BadInputDataException("These users can't have a chat");
        }

        if (firstUser.getChats().stream()
                .anyMatch(chat -> secondUser.getChats().contains(chat))) {
            throw new BadInputDataException("These users already have a chat");
        }

        Chat chat = chatMapper.toChat(chatDto);

//        Set<User> users = new HashSet<>() {
//            {
//                add(firstUser);
//                add(secondUser);
//            }
//        };

        chat.setUsers(Set.of(firstUser,secondUser));
        chat.setCreatedAt(LocalDateTime.now());

        chatRepository.save(chat);
        return chatMapper.toDto(chat);
    }

    @Override
    public void deleteChatById(Long chatId) {
        checkAccessByChatId(chatId);
        chatRepository.findById(chatId).orElseThrow(ChatNotFoundException::new);
        chatRepository.deleteById(chatId);
    }

    @Override
    public Long joinChat(Long chatId) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByName(name).orElseThrow(UserNotFoundException::new);

        Chat chat = chatRepository.findById(chatId).orElseThrow(ChatNotFoundException::new);

        if (chat.getUsers().size() == 2) {
            throw new BadInputDataException("There are already two users in the chat");
        }

        User oneManChat = chat.getUsers().stream().findFirst().orElseThrow(UserNotFoundException::new);

        if (!oneManChat.getFriends().contains(user)) {
            throw new BadInputDataException("This user can't join in this chat");
        }

        chat.getUsers().add(user);

        chatRepository.save(chat);
        return user.getId();
    }

    @Override
    public Long leaveChat(Long chatId) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByName(name).orElseThrow(UserNotFoundException::new);

        ChatDto chatDto = getChatById(chatId);
        Chat chat = chatMapper.toChat(chatDto);

        chat.getUsers().remove(user);
        if (chat.getUsers().isEmpty()) {
            deleteChatById(chatId);
        } else {
            chatRepository.save(chat);
        }
        return user.getId();
    }

    @Transactional(readOnly = true)
    @Override
    public String getLastMessage(Long chatId) {
        checkAccessByChatId(chatId);
        return messageRepository.getLastMessageFromChat(chatId);
    }

    @Transactional(readOnly = true)
    @Override
    public String getLastMessageFromMessageDto(Long chatId) {
        return messageRepository.getLastMessageFromChat(chatId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MessageDto> getAllMessagesInChat(Long chatId) {
        checkAccessByChatId(chatId);
        return messageMapper.toListDto(messageRepository.getAllMessagesInChat(chatId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatDto> getAllChats() {
        checkAccessByAdmin();
        return chatMapper.toListDto(chatRepository.findAll());
    }

    public void checkAccessByChatId(Long chatId) throws AccessDeniedException, ChatNotFoundException {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Chat chat = chatRepository.findById(chatId).orElseThrow(ChatNotFoundException::new);
        Set<User> users = chat.getUsers();
        if (users.stream().map(User::getName).noneMatch(x -> x.equals(name))) {
            throw new AccessDeniedException("Error: access denied!");
        }
    }

    public void checkAccessByAdmin() throws AccessDeniedException {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        List<String> allNameAdmins = userRepository.getAllNameAdmins();
        if (allNameAdmins.stream().noneMatch(x -> x.equals(name))) {
            throw new AccessDeniedException("Error: access denied!");
        }
    }
}
