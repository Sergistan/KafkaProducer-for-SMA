package com.utochkin.kafkaproducerforsma.security;


import com.utochkin.kafkaproducerforsma.exceptions.UserNotFoundException;
import com.utochkin.kafkaproducerforsma.models.User;
import com.utochkin.kafkaproducerforsma.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(final String name) {
        User user = userService.findByName(name);
        return JwtEntityFactory.create(user);
    }

}
