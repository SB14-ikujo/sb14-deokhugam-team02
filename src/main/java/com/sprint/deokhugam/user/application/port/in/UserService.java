package com.sprint.deokhugam.user.application.port.in;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasher passwordHasher;
    private final UserMapper userMapper;
    private static final Duration HARD_DELETE_GRACE_PERIOD = Duration.ofDays(1);

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
            User savedUser = userRepository.saveAndFlush(user);
            log.info("회원가입 완료: userId={}", savedUser.getId());
            return userMapper.toDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            // 동시 가입으로 existsBy 검사를 통과한 뒤 유니크 제약에 걸린 경우만 중복으로 변환한다.
            if (isUniqueViolation(e, User.EMAIL_UNIQUE_CONSTRAINT)) {
                throw new UserException(UserErrorCode.USER_EMAIL_DUPLICATE);
            }
            if (isUniqueViolation(e, User.NICKNAME_UNIQUE_CONSTRAINT)) {
                throw new UserException(UserErrorCode.USER_NICKNAME_DUPLICATE);
            }
            throw e;
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
        log.info("로그인 성공: userId={}", user.getId());
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
        validateOwner(userId, requesterId);

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
            User savedUser = userRepository.saveAndFlush(user);
            log.info("닉네임 수정 완료: userId={}", savedUser.getId());
            return userMapper.toDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 검증을 통과한 뒤 닉네임 유니크 제약에 걸린 경우만 중복으로 변환한다.
            if (isUniqueViolation(e, User.NICKNAME_UNIQUE_CONSTRAINT)) {
                throw new UserException(UserErrorCode.USER_NICKNAME_DUPLICATE);
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId, UUID requesterId) {
        validateOwner(userId, requesterId);

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        user.delete();
        log.info("사용자 논리 삭제 처리: userId={}", userId);
    }

    @Override
    @Transactional
    public void hardDeleteUser(UUID userId, UUID requesterId) {
        validateOwner(userId, requesterId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
        log.info("사용자 물리 삭제 처리: userId={}", userId);
    }

    @Override
    @Transactional
    public int deleteExpiredUsers(Instant now) {
        return userRepository.deleteSoftDeletedUsersBefore(now.minus(HARD_DELETE_GRACE_PERIOD));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateOwner(UUID userId, UUID requesterId) {
        if (!userId.equals(requesterId)) {
            throw new UserException(UserErrorCode.USER_FORBIDDEN);
        }
    }

    private boolean isUniqueViolation(DataIntegrityViolationException exception, String constraintName) {
        return exception.getCause() instanceof ConstraintViolationException cve
            && cve.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE
            && constraintName.equals(cve.getConstraintName());
    }
}
