package com.greencore.versioncontrol.service;

import com.greencore.versioncontrol.model.User;
import com.greencore.versioncontrol.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void testCreateUser(){
        User user = new User();
        user.setUsername("jimin3");
        user.setPassword("password3");
        user.setEmail("jimin3@naver.com");

        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");

        // 서비스 메서드 호출
        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertEquals("jimin", createdUser.getUsername());
        assertEquals("jimin@naver.com", createdUser.getEmail());
    }
}
