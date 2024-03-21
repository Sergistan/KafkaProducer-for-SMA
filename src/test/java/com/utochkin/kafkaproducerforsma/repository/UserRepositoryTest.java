package com.utochkin.kafkaproducerforsma.repository;

import com.utochkin.kafkaproducerforsma.models.Role;
import com.utochkin.kafkaproducerforsma.models.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void findByName() {
        User user = User.builder()
                .id(1L)
                .name("Sergey")
                .password(passwordEncoder.encode("111"))
                .email("sergistan.utochkin@yandex.ru")
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);

        User userFromRepository = userRepository.findByName("Sergey").get();
        Assertions.assertEquals(userFromRepository, savedUser);
    }

    @Test
    void findByNameUserNotFound() {
        Assertions.assertEquals(Optional.empty(), userRepository.findByName("Kate"));
    }


    @Test
    void getAllNameAdmins() {
        User userAdmin1 = User.builder()
                .id(4L)
                .name("Tom")
                .password(passwordEncoder.encode("444"))
                .email("tom@ya.com")
                .role(Role.ROLE_ADMIN)
                .build();

        userRepository.save(userAdmin1);

        User userAdmin2 = User.builder()
                .id(5L)
                .name("Kate")
                .password(passwordEncoder.encode("555"))
                .email("kate@ya.com")
                .role(Role.ROLE_ADMIN)
                .build();

        userRepository.save(userAdmin2);

        List<String> allNameAdmins = userRepository.getAllNameAdmins();

        Assertions.assertTrue(allNameAdmins.containsAll(List.of(userAdmin1.getName(),userAdmin2.getName())));
    }

    @Test
    void getAllNameAdminsNotExistAdmins() {
        Optional<User> user = userRepository.findByName("Tom");
        if(user.isPresent()){
            userRepository.delete(user.get());
        }

        List<String> allNameAdmins = userRepository.getAllNameAdmins();

        Assertions.assertEquals(Collections.emptyList(), allNameAdmins);
    }
}