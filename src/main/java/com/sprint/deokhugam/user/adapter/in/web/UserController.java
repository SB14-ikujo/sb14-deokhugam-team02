package com.sprint.deokhugam.user.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.sprint.deokhugam.common.context.user.RequestUserId;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserDto;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserLoginRequest;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserRegisterRequest;
import com.sprint.deokhugam.user.adapter.in.web.dto.UserUpdateRequest;
import com.sprint.deokhugam.user.application.port.in.UserUseCase;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserUseCase userUseCase;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserDto register(@Valid @RequestBody UserRegisterRequest request) {
        return userUseCase.register(request);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login")
    public UserDto login(@Valid @RequestBody UserLoginRequest request) {
        return userUseCase.login(request);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}")
    public UserDto findUser(@PathVariable UUID userId) {
        return userUseCase.findUser(userId);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{userId}")
    public UserDto updateNickname(
        @PathVariable UUID userId,
        @Parameter(hidden = true) @RequestUserId UUID requesterId,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        return userUseCase.updateNickname(userId, requesterId, request);
    }
}
