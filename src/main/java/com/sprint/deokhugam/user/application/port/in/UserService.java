package com.sprint.deokhugam.user.application.port.in;

import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.user.adapter.in.web.dto.UserDto;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserLoginRequest;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserRegisterRequest;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserUpdateRequest;
import com.sprint.deokhugam.user.adapter.in.web.mapper.UserMapper;
import com.sprint.deokhugam.user.application.port.out.PasswordHasher;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;
import com.sprint.deokhugam.user.domain.User;
import com.sprint.deokhugam.user.domain.exception.UserErrorCode;
import com.sprint.deokhugam.user.domain.exception.UserException;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasher passwordHasher;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto register(UserRegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new UserException(UserErrorCode.USER_EMAIL_DUPLICATE);
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new UserException(UserErrorCode.USER_NICKNAME_DUPLICATE);
        }

        User user = User.create(email, request.nickname(), passwordHasher.hash(request.password()));
        try {
            return userMapper.toDto(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException e) {
            // 동시 가입으로 existsBy 검사를 통과한 뒤 데이터베이스의 유니크 제약에 걸린 경우.
            throw new UserException(UserErrorCode.USER_DUPLICATE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto login(UserLoginRequest request) {
        User user = userRepository.findByEmailAndIsDeletedFalse(normalizeEmail(request.email()))
            .orElseThrow(() -> new UserException(UserErrorCode.USER_LOGIN_FAILED));

        if (!passwordHasher.matches(request.password(), user.getPassword())) {
            throw new UserException(UserErrorCode.USER_LOGIN_FAILED);
        }
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto findUser(UUID userId) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto updateNickname(UUID userId, UUID requesterId, UserUpdateRequest request) {
        if (!userId.equals(requesterId)) {
            throw new UserException(UserErrorCode.USER_FORBIDDEN);
        }

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String newNickname = request.nickname();
        if (newNickname.equals(user.getNickname())) {
            return userMapper.toDto(user);
        }
        if (userRepository.existsByNickname(newNickname)) {
            throw new UserException(UserErrorCode.USER_NICKNAME_DUPLICATE);
        }

        user.changeNickname(newNickname);
        try {
            return userMapper.toDto(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 검증을 통과한 뒤 DB 유니크 제약에 걸린 경우
            throw new UserException(UserErrorCode.USER_NICKNAME_DUPLICATE);
        }
    }

    @Override
    public void deleteUser(UUID userId, UUID requesterId) {
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
