package com.greencore.versioncontrol.controller;

import com.greencore.versioncontrol.controller.LoginController.JwtResponse;
import com.greencore.versioncontrol.model.User;
import com.greencore.versioncontrol.repository.UserRepository;
import com.greencore.versioncontrol.service.UserService;
import com.greencore.versioncontrol.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_JWT = "test_jwt_token";
    private static final String AUTHENTICATION_FAILED_MESSAGE = "Authentication failed";

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserService userService;
    @Mock
    private Authentication authentication;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    void setUp() {

        // MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    private User createTestUser() {
        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_PASSWORD);
        return user;
    }

    @Test
    @DisplayName("Should return JWT token on successful authentication")
    void createAuthenticationToken_Success() {
        User testUser = createTestUser();
        UserDetails userDetails = mock(UserDetails.class);

        when(userDetails.getUsername()).thenReturn(testUser.getUsername());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userService.loadUserByUsername(testUser.getUsername())).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails.getUsername())).thenReturn(TEST_JWT);

        ResponseEntity<?> response = loginController.createAuthenticationToken(testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof JwtResponse);
        assertEquals(TEST_JWT, ((JwtResponse) response.getBody()).getJwt());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).loadUserByUsername(testUser.getUsername());
        verify(jwtUtil).generateToken(testUser.getUsername());
    }

    @Test
    @DisplayName("Should return unauthorized on failed authentication")
    void createAuthenticationToken_Failure() {
        User testUser = createTestUser();
        testUser.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        ResponseEntity<?> response = loginController.createAuthenticationToken(testUser);

        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(AUTHENTICATION_FAILED_MESSAGE, response.getBody());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Should return current user details when user is authenticated")
    void getCurrentUser_AuthenticatedUser() {
        UserDetails userDetails = mock(UserDetails.class);

        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(TEST_USERNAME);
        when(userService.loadUserByUsername(TEST_USERNAME)).thenReturn(userDetails);

        ResponseEntity<?> response = loginController.getCurrentUser();

        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof UserDetails);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDetails returnedUserDetails = (UserDetails) response.getBody();
        assertEquals(TEST_USERNAME, returnedUserDetails.getUsername());
        verify(securityContext).getAuthentication();
        verify(userService).loadUserByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should return not found when user is not found")
    void getCurrentUser_UserNotFound() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(TEST_USERNAME);
        when(userService.loadUserByUsername(TEST_USERNAME)).thenThrow(new UsernameNotFoundException("User not found"));

        ResponseEntity<?> response = loginController.getCurrentUser();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService).loadUserByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should return bad request when authentication is null")
    void getCurrentUser_NullAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);

        ResponseEntity<?> response = loginController.getCurrentUser();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid authentication", response.getBody());
        verify(userService, never()).loadUserByUsername(anyString()); // 어떤 문자열 인자(anyString())로도 절대 호출되지 않았음(never())
    }
}