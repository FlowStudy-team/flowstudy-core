package com.flowstudy.core.module.auth.service;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.module.auth.dto.LoginRequest;
import com.flowstudy.core.module.auth.dto.RegisterRequest;
import com.flowstudy.core.module.auth.vo.LoginResponse;
import com.flowstudy.core.module.auth.vo.RegisterResponse;
import com.flowstudy.core.module.user.entity.User;
import com.flowstudy.core.module.user.mapper.UserMapper;
import com.flowstudy.core.module.user.vo.UserResponse;
import com.flowstudy.core.security.AuthenticatedUser;
import com.flowstudy.core.security.JwtTokenProvider;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = normalizeEmail(request.email());
        if (userMapper.findByUsername(username) != null) {
            throw new BusinessException(41002, "username already exists", HttpStatus.CONFLICT);
        }
        if (email != null && userMapper.findByEmail(email) != null) {
            throw new BusinessException(41003, "email already exists", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(normalizeNickname(request.nickname(), username));

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(40900, "user account already exists", HttpStatus.CONFLICT);
        }
        return new RegisterResponse(user.getId());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String account = request.account().trim();
        if (account.contains("@")) {
            account = account.toLowerCase(Locale.ROOT);
        }
        User user = userMapper.findByAccount(account);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(41001, "username or password is incorrect", HttpStatus.UNAUTHORIZED);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(41004, "user is disabled", HttpStatus.FORBIDDEN);
        }

        userMapper.updateLastLoginAt(user.getId());
        String token = tokenProvider.createToken(new AuthenticatedUser(user.getId(), user.getUsername(), user.getRole()));
        return new LoginResponse(token, "Bearer", tokenProvider.getExpireSeconds(), UserResponse.from(user));
    }

    public UserResponse getCurrentUser(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(41000, "user does not exist", HttpStatus.NOT_FOUND);
        }
        return UserResponse.from(user);
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNickname(String nickname, String username) {
        return nickname == null || nickname.isBlank() ? username : nickname.trim();
    }
}
