package com.utochkin.kafkaproducerforsma.services.impl;

import com.utochkin.kafkaproducerforsma.dto.UserDto;
import com.utochkin.kafkaproducerforsma.exceptions.BadInputDataException;
import com.utochkin.kafkaproducerforsma.exceptions.ChatNotFoundException;
import com.utochkin.kafkaproducerforsma.exceptions.UserNotFoundException;
import com.utochkin.kafkaproducerforsma.mappers.UserMapper;
import com.utochkin.kafkaproducerforsma.models.Chat;
import com.utochkin.kafkaproducerforsma.models.Role;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.repository.UserRepository;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        user.setRole(Role.ROLE_USER);
        return userRepository.save(user);
    }

    @Override
    public void createFriendRequest(Long userIdFrom, Long userIdTo) {
        User userFrom = userRepository.findById(userIdFrom).orElseThrow(UserNotFoundException::new);
        User userTo = userRepository.findById(userIdTo).orElseThrow(UserNotFoundException::new);
        if (userFrom.getFriends().contains(userTo)) {
            throw new BadInputDataException(String.format("User with id = %s already have friend with id = %s", userIdFrom, userIdTo));
        }
        userTo.addFollower(userFrom);
    }

    @Override
    public void acceptFriendRequest(Long userIdFrom, Long userIdAccepted) {
        User userFrom = userRepository.findById(userIdFrom).orElseThrow(UserNotFoundException::new);
        User userAcceptedRequest = userRepository.findById(userIdAccepted).orElseThrow(UserNotFoundException::new);
        if (userFrom.getFriends().contains(userAcceptedRequest)) {
            throw new BadInputDataException(String.format("User with id = %s already friend with id = %s", userIdFrom, userIdAccepted));
        }
        userAcceptedRequest.addFriend(userFrom);
        userFrom.addFollower(userAcceptedRequest);
    }

    @Override
    public void refuseFriendRequest(Long userIdFrom, Long userIdRefused) {
        User userFrom = userRepository.findById(userIdFrom).orElseThrow(UserNotFoundException::new);
        User userRefusedRequest = userRepository.findById(userIdRefused).orElseThrow(UserNotFoundException::new);
        if (userFrom.getFriends().contains(userRefusedRequest)) {
            throw new BadInputDataException(String.format("User with id = %s already accept friend request user with id = %s", userIdFrom, userIdRefused));
        }
        userRefusedRequest.addFollower(userFrom);
    }

    @Override
    public void refuseFollower(Long userIdFollower, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        User follower = userRepository.findById(userIdFollower).orElseThrow(UserNotFoundException::new);
        if (!user.getFollowers().contains(follower)) {
            throw new BadInputDataException(String.format("User with id = %s not have follower with id = %s", userId, userIdFollower));
        }
        user.deleteFollower(follower);
    }

    @Override
    public void deleteFriend(Long userId, Long userIdDeleted) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        User userDeleted = userRepository.findById(userIdDeleted).orElseThrow(UserNotFoundException::new);
        if (!user.getFriends().contains(userDeleted)) {
            throw new BadInputDataException(String.format("User with id = %s not have friend with id = %s", userId, userIdDeleted));
        }
        user.deleteFriend(userDeleted);
        user.deleteFollower(userDeleted);

        if (user.getChats().stream()
                .anyMatch(chat -> userDeleted.getChats().contains(chat))){
            Chat chat = user.getChats().stream().filter(x -> x.getUsers().contains(userDeleted)).findFirst().orElseThrow(ChatNotFoundException::new);
            chatService.deleteChatById(chat.getId());
        }
    }


}
