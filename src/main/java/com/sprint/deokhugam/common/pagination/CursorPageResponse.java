package com.sprint.deokhugam.common.pagination;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Slice;

public record CursorPageResponse<T>(
    List<T> content,
    String nextCursor,
    Instant nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

    /**
     * Slice와 전체 개수를 받아서 커서 페이지 응답으로 바꿔준다.
     * 다음 페이지가 있으면 마지막 항목에서 nextCursor, nextAfter를 꺼내고,
     * 없으면 둘 다 null로 둔다.
     * Extractor를 사용하는 이유: 제네릭 T라서 내부에선 어떤 필드가 있는지 알 수 없어, 커서 값을 꺼내는 방법을 외부에서 람다로 전달한다.
     *
     * @param slice           현재 페이지 데이터와 다음 페이지 여부가 담긴 Slice
     * @param totalElements   전체 데이터 개수
     * @param cursorExtractor 마지막 항목에서 nextCursor 값을 꺼내는 함수
     * @param afterExtractor  마지막 항목에서 nextAfter 값을 꺼내는 함수
     * @return 커서 페이지 응답
     * @param <T>             데이터 타입 (예: NotificationDto, ReviewDto)
     */
    public static <T> CursorPageResponse<T> of(
        Slice<T> slice,
        long totalElements,
        Function<T, String> cursorExtractor,
        Function<T, Instant> afterExtractor
    ) {
        List<T> content = slice.getContent();
        T last = slice.hasNext() ? content.get(content.size() - 1) : null;

        return new CursorPageResponse<>(
            content,
            last != null ? cursorExtractor.apply(last) : null,
            last != null ? afterExtractor.apply(last) : null,
            slice.getSize(),
            totalElements,
            slice.hasNext()
        );
    }
}
