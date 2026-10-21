package com.sprint.deokhugam.user.adapter.in.web.mapper;

import org.mapstruct.Mapper;

import com.sprint.deokhugam.user.adapter.in.web.dto.UserDto;
import com.sprint.deokhugam.user.domain.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
