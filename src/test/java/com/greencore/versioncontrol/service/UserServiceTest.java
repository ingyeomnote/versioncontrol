package com.greencore.versioncontrol.service;

import com.greencore.versioncontrol.model.User;
import com.greencore.versioncontrol.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Test
    public void testCreateUser(){
        User user = createUserTest();
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        //서비스 메서드 호출
        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertEquals("jimin", createdUser.getUsername());
        assertEquals("jimin@naver.com", createdUser.getEmail());
        assertEquals("encodedPassword", createdUser.getPassword());

        verify(userRepository, times(1)).save(any(User.class));
    }

    private User createUserTest() {
        User user = new User();
        user.setUsername("jimin");
        user.setPassword("password");
        user.setEmail("jimin@naver.com");
        return user;
    }
}
