package com.sprint.deokhugam.notification.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sprint.deokhugam.notification.adapter.in.web.dto.NotificationDto;
import com.sprint.deokhugam.notification.domain.Notification;

@Mapper(componentModel = "spring")    // Spring Bean으로 등록
public interface NotificationMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "review.id", target = "reviewId")
    @Mapping(source = "review.content", target = "reviewContent")
    NotificationDto toDto(Notification notification);
}
