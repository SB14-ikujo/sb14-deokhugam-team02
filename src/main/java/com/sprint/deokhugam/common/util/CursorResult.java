package com.sprint.deokhugam.common.util;

import java.util.List;

/**
 * 커서 페이지네이션 조회 결과를 담는 객체.
 *
 * @param content 현재 페이지에 보여줄 데이터 목록
 * @param hasNext 다음 페이지가 있는지 여부
 * @param <T>     데이터 타입 (예: Notification, Review)
 */
public record CursorResult<T>(List<T> content, boolean hasNext) {

    /**
     * limit + 1개로 조회한 결과를 받아서 다음 페이지 여부를 판단하고,
     * 확인용으로 더 가져온 마지막 1개를 제거해서 반환한다.
     *
     * @param result limit + 1개로 조회한 결과
     * @param limit  한 페이지에 보여줄 개수
     * @param <T>    데이터 타입
     * @return 다음 페이지 여부가 담긴 조회 결과
     */
    public static <T> CursorResult<T> of(List<T> result, int limit) {
        boolean hasNext = result.size() > limit;
        if (hasNext) {
            result.remove(result.size() - 1);
        }
        return new CursorResult<>(result, hasNext);
    }
}
