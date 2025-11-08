package com.aitravelplanner.backend.service;

import com.aitravelplanner.backend.model.User;
import com.aitravelplanner.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 注册新用户
    public User registerUser(String email, String password) {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        // 创建新用户
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        return userRepository.save(user);
    }

    // 根据邮箱查找用户
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 检查用户是否存在
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}