package com.aitravelplanner.backend.controller;

import com.aitravelplanner.backend.dto.AuthResponse;
import com.aitravelplanner.backend.dto.LoginRequest;
import com.aitravelplanner.backend.dto.RegisterRequest;
import com.aitravelplanner.backend.model.User;
import com.aitravelplanner.backend.security.JwtUtil;
import com.aitravelplanner.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // 用户注册端点
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            // 注册用户
            User user = userService.registerUser(registerRequest.getEmail(), registerRequest.getPassword());
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Email already exists", HttpStatus.BAD_REQUEST);
        }
    }

    // 用户登录端点
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login attempt for email: {}", loginRequest.getEmail());
            
            // 验证用户凭证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // 设置认证信息
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成JWT token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            logger.info("Authentication successful, generating JWT for user: {}", userDetails.getUsername());
            
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            logger.info("JWT generated successfully");

            // 返回token
            return ResponseEntity.ok(new AuthResponse(jwt, userDetails.getUsername()));
        } catch (Exception e) {
            logger.error("Login error: ", e);
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }
}