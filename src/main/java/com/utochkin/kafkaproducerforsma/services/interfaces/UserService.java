package com.utochkin.kafkaproducerforsma.services.interfaces;


import com.utochkin.kafkaproducerforsma.dto.UserDto;
import com.utochkin.kafkaproducerforsma.models.User;

public interface UserService {

    User findByName(String name);

    User getById(Long id);

    User createUser(UserDto userDto);

    void createFriendRequest(Long userIdFrom, Long userIdTo);

    void acceptFriendRequest(Long userIdFrom, Long userIdAccepted);

    void refuseFriendRequest(Long userIdRefused, Long userIdFrom);

    void refuseFollower(Long userIdFollower, Long userId);

    void deleteFriend(Long userId, Long userIdDeleted);
}
