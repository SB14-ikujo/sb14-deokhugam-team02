package com.sprint.deokhugam.user.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.user.adapter.in.web.dto.UserDto;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserRegisterRequest;
import com.sprint.deokhugam.user.application.port.in.UserUseCase;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/users")
    public UserDto register(@Valid @RequestBody UserRegisterRequest request) {
        return userUseCase.register(request);
    }
}
