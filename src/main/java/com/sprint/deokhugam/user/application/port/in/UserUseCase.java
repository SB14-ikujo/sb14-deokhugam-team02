package com.sprint.deokhugam.user.application.port.in;

import java.util.UUID;

import com.sprint.deokhugam.user.adapter.in.web.dto.UserDto;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserLoginRequest;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserRegisterRequest;

public interface UserUseCase {

    UserDto register(UserRegisterRequest request);

    UserDto login(UserLoginRequest request);

    UserDto findUser(UUID userId);

}
