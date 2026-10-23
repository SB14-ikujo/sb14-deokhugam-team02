package com.sprint.deokhugam.common.util;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SliceUtils {
    /**
     * limit + 1개로 조회한 결과를 Slice로 바꿔준다.
     * 결과가 limit보다 많으면 다음 페이지가 있다고 보고,
     * 확인용으로 더 가져온 마지막 1개를 제거한다.
     *
     * @param result limit + 1개로 조회한 결과
     * @param limit  한 페이지에 보여줄 개수
     * @return 데이터와 다음 페이지 여부가 담긴 Slice
     * @param <T>    데이터 타입 (예: NotificationDto, ReviewDto)
     */
    public static <T> Slice<T> toSlice(List<T> result, int limit) {
        if (result.isEmpty()) {
            return new SliceImpl<>(List.of(), Pageable.ofSize(limit), false);
        }

        boolean hasNext = result.size() > limit;
        List<T> content = hasNext ? result.subList(0, limit) : result;

        return new SliceImpl<>(content, Pageable.ofSize(limit), hasNext);
    }
}

