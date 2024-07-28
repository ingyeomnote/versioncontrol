package com.greencore.versioncontrol.controller;

import com.greencore.versioncontrol.model.User;
import com.greencore.versioncontrol.service.UserService;
import com.greencore.versioncontrol.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> createAuthenticationToken(@RequestBody User user){
      try {
          // 인증 로직
          Authentication authentication = authenticationManager.authenticate(
                  new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
          );

          // 사용자 체크
          final UserDetails userDetails = userService.loadUserByUsername(user.getUsername());

          // 토큰 생성
          final String jwt = jwtUtil.generateToken(userDetails.getUsername());

          System.out.println("Login successful for user : " + user.getUsername());
          System.out.println("Generate token : " + jwt);

          return ResponseEntity.ok(new JwtResponse(jwt));
      } catch (AuthenticationException e){
          System.out.println("Login failed for user: " + user.getUsername() + e);
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
      }
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
        private final String jwt;

        public JwtResponse(String jwt) {
            this.jwt = jwt;
        }

        public String getJwt(){
            return jwt;
        }
    }
}