package com.greencore.versioncontrol.controller;

import com.greencore.versioncontrol.model.User;
import com.greencore.versioncontrol.service.UserService;
import com.greencore.versioncontrol.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class LoginController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public LoginController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userService.createUser(user) != null) {
            return ResponseEntity.ok("User registered successfully");
        }
        return ResponseEntity.badRequest().body("User registration failed");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user){
        User authenticatedUser = userService.authenticateUser(user.getUsername(), user.getPassword());
        if(authenticatedUser != null){
            String token = jwtUtil.generateToken(authenticatedUser.getUsername());
            return ResponseEntity.ok(new JwtResponse(token));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    public class JwtResponse {
        private String token;
        public JwtResponse(String token) {
            this.token = token;
        }
        public String getToken(){
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
