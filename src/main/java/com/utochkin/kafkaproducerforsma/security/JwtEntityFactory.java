package com.utochkin.kafkaproducerforsma.security;


import com.utochkin.kafkaproducerforsma.models.User;

public class JwtEntityFactory {

    public static JwtEntity create(final User user) {
        return new JwtEntity(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }

}
