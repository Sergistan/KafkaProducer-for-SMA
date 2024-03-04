package com.utochkin.kafkaproducerforsma.services.impl;

import com.utochkin.kafkaproducerforsma.dto.UserDto;
import com.utochkin.kafkaproducerforsma.exceptions.AccessDeniedException;
import com.utochkin.kafkaproducerforsma.exceptions.BadInputDataException;
import com.utochkin.kafkaproducerforsma.exceptions.ChatNotFoundException;
import com.utochkin.kafkaproducerforsma.exceptions.UserNotFoundException;
import com.utochkin.kafkaproducerforsma.mappers.UserMapper;
import com.utochkin.kafkaproducerforsma.models.Chat;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.repository.UserRepository;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final ChatServiceImpl chatService;

    @Transactional(readOnly = true)
    @Override
    public User findByName(String name) {
        return userRepository.findByName(name).orElseThrow(UserNotFoundException::new);
    }

    @Transactional(readOnly = true)
    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public User createUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Long createFriendRequest(Long userIdTo) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User userFrom = userRepository.findByName(name).orElseThrow(UserNotFoundException::new);
        checkingTheRequestToYourself(userFrom, userIdTo);

        User userTo = userRepository.findById(userIdTo).orElseThrow(UserNotFoundException::new);
        if (userFrom.getFriends().contains(userTo)) {
            throw new BadInputDataException(String.format("User with id = %s already have friend with id = %s", userFrom.getId(), userIdTo));
        }
        userTo.addFollower(userFrom);
        userTo.addFriendRequest(userFrom);
        return userFrom.getId();
    }


    @Override
    public Long acceptFriendRequest(Long userIdSendedRequest) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User userAccepted = userRepository.findByName(name).orElseThrow(UserNotFoundException::new);
        checkingTheRequestToYourself(userAccepted, userIdSendedRequest);

        User userSendedRequest = userRepository.findById(userIdSendedRequest).orElseThrow(UserNotFoundException::new);
        ifExistFriendRequestThenDeleteFriendRequest(userAccepted, userSendedRequest);

        if (userAccepted.getFriends().contains(userSendedRequest)) {
            throw new BadInputDataException(String.format("User with id = %s already friend with id = %s", userAccepted.getId(), userIdSendedRequest));
        }
        userAccepted.addFriend(userSendedRequest);
        userSendedRequest.addFollower(userAccepted);
        return userAccepted.getId();
    }

    @Override
    public Long refuseFriendRequest(Long userIdSendedRequest) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User userRefusedRequest = userRepository.findByName(name).orElseThrow(UserNotFoundException::new);
        checkingTheRequestToYourself(userRefusedRequest, userIdSendedRequest);

        User userSendedRequest = userRepository.findById(userIdSendedRequest).orElseThrow(UserNotFoundException::new);
        ifExistFriendRequestThenDeleteFriendRequest(userRefusedRequest, userSendedRequest);

        if (userSendedRequest.getFriends().contains(userRefusedRequest)) {
            throw new BadInputDataException(String.format("User with id = %s already accept friend request user with id = %s", userRefusedRequest.getId(), userIdSendedRequest));
        }
        return userRefusedRequest.getId();
    }

    @Override
    public Long refuseFollower(Long userId) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User userRefused = userRepository.findByName(name).orElseThrow(UserNotFoundException::new);
        checkingTheRequestToYourself(userRefused, userId);

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (!user.getFollowers().contains(userRefused)) {
            throw new BadInputDataException(String.format("User with id = %s not have follower on id = %s", userId, userRefused.getId()));
        }
        user.deleteFollower(userRefused);
        return userRefused.getId();
    }

    @Override
    public Long deleteFriend(Long userIdDeleted) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByName(name).orElseThrow(UserNotFoundException::new);
        checkingTheRequestToYourself(user, userIdDeleted);

        User userDeleted = userRepository.findById(userIdDeleted).orElseThrow(UserNotFoundException::new);
        if (!user.getFriends().contains(userDeleted)) {
            throw new BadInputDataException(String.format("User with id = %s not have friend with id = %s", user.getId(), userIdDeleted));
        }
        user.deleteFriend(userDeleted);
        userDeleted.deleteFollower(user);

        if (user.getChats().stream()
                .anyMatch(chat -> userDeleted.getChats().contains(chat))) {
            Chat chat = user.getChats().stream().filter(x -> x.getUsers().contains(userDeleted)).findFirst().orElseThrow(ChatNotFoundException::new);
            chatService.deleteChatById(chat.getId());
        }
        return user.getId();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsers() {
        checkAccessByAdmin();
        return userMapper.toListDto(userRepository.findAll());
    }

    @Transactional(readOnly = true)
    @Override
    public Long getIdUser(String username) {
        User user = userRepository.findByName(username).orElseThrow(UserNotFoundException::new);
        return user.getId();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsersFriends() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByName(name).orElseThrow(UserNotFoundException::new);
        if (!user.getFriends().isEmpty()) {
            return userMapper.toListDto(user.getFriends().stream().toList());
        }
        return Collections.emptyList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsersFollowers() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByName(name).orElseThrow(UserNotFoundException::new);
        if (!user.getFollowers().isEmpty()) {
            return userMapper.toListDto(user.getFollowers().stream().toList());
        }
        return Collections.emptyList();
    }

    public void checkAccessByAdmin() throws AccessDeniedException {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        List<String> allNameAdmins = userRepository.getAllNameAdmins();
        if (allNameAdmins.stream().noneMatch(x -> x.equals(name))) {
            throw new AccessDeniedException("Error: access denied!");
        }
    }

    private void checkingTheRequestToYourself(User user, Long userIdRequest) {
        if (user.getId().equals(userIdRequest)) {
            throw new BadInputDataException("You can't send a request to yourself");
        }
    }

    private void ifExistFriendRequestThenDeleteFriendRequest(User user, User userSendedRequest) {
        if (user.getFriendRequests().contains(userSendedRequest)) {
            user.deleteFriendRequest(userSendedRequest);
        } else
            throw new BadInputDataException(String.format("You have not friend request from %s", userSendedRequest.getName()));
    }
}
