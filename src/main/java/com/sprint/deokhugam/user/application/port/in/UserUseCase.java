package com.sprint.deokhugam.user.application.port.in;

import com.sprint.deokhugam.user.adapter.in.web.dto.UserDto;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserRegisterRequest;

public interface UserUseCase {

    UserDto register(UserRegisterRequest request);
}
