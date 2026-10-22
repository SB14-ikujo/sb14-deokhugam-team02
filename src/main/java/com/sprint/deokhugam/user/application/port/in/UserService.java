package com.sprint.deokhugam.user.application.port.in;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.user.adapter.in.web.dto.UserDto;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserRegisterRequest;
import com.sprint.deokhugam.user.adapter.in.web.mapper.UserMapper;
import com.sprint.deokhugam.user.application.port.out.PasswordHasher;
import com.sprint.deokhugam.user.application.port.out.UserRepositoryPort;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepository;

    private final PasswordHasher passwordHasher;

    private final UserMapper userMapper;

    @Override
    public UserDto register(UserRegisterRequest request) {
        return null;
    }
}
