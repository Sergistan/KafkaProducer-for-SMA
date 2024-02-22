package com.utochkin.kafkaproducerforsma.services.interfaces;


import com.utochkin.kafkaproducerforsma.dto.UserDto;
import com.utochkin.kafkaproducerforsma.models.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User findByName(String name);

    User getById(Long id);

    User createUser(UserDto userDto);

    Long createFriendRequest(Long userIdTo);

    Long acceptFriendRequest(Long userIdSendedRequest);

    Long refuseFriendRequest(Long userIdSendedRequest);

    Long refuseFollower(Long userId);

    Long deleteFriend(Long userIdDeleted);

    List<UserDto> getAllUsers();

    Long getIdUser(String username);

    List<UserDto> getAllUsersFriends();

    List<UserDto> getAllUsersFollowers();
}
