package com.greencore.versioncontrol.controller;

import com.greencore.versioncontrol.model.User;
import com.greencore.versioncontrol.service.UserService;
import com.greencore.versioncontrol.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public LoginController(AuthenticationManager authenticationManager, UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        User registeredUser = userService.registerNewUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody User user, HttpServletResponse response){
        try {
            // 인증 로직
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );

            // 사용자 체크
            final UserDetails userDetails = userService.loadUserByUsername(user.getUsername());

            // 토큰 생성
            final String accessToken = jwtUtil.generateToken(userDetails.getUsername());

            final String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

            // 리프레시 토큰을 Http-only 쿠키로 설정
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true) // HTTPS를 사용하는 경우에만 true로 설정
                    .path("/api/users/refresh-token")
                    .maxAge(7 * 24 * 60 * 60) // 7일
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            return ResponseEntity.ok(new JwtResponse(accessToken));
        } catch (AuthenticationException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request){
        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);
        if(refreshToken != null && jwtUtil.validateRefreshToken(refreshToken)){
            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userService.loadUserByUsername(username);
            String newAccessToken = jwtUtil.generateToken(userDetails.getUsername());
            return ResponseEntity.ok(new JwtResponse((newAccessToken)));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserDetails userDetails = userService.loadUserByUsername(username);

            if (userDetails != null) {
                return ResponseEntity.ok(userDetails);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body("Invalid authentication");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public class JwtResponse {
        private final String accessToken;

        public JwtResponse(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }
}