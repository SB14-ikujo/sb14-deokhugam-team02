package com.sprint.deokhugam.common.pagination;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

class CursorPageResponseTest {

    private record Item(String id, Instant createdAt) {
    }

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2026-01-02T00:00:00Z");

    @Test
    @DisplayName("다음 페이지가 있으면 마지막 항목에서 nextCursor, nextAfter를 꺼낸다")
    void of_hasNext() {
        // given
        List<Item> items = List.of(new Item("a", T1), new Item("b", T2));
        SliceImpl<Item> slice = new SliceImpl<>(items, Pageable.ofSize(2), true);

        // when
        CursorPageResponse<Item> response = CursorPageResponse.of(slice, 10L, Item::id, Item::createdAt);

        // then
        assertThat(response.content()).isEqualTo(items);
        assertThat(response.nextCursor()).isEqualTo("b");
        assertThat(response.nextAfter()).isEqualTo(T2);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(10L);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    @DisplayName("다음 페이지가 없으면 nextCursor, nextAfter는 null")
    void of_noNext() {
        List<Item> items = List.of(new Item("a", T1));
        SliceImpl<Item> slice = new SliceImpl<>(items, Pageable.ofSize(5), false);

        CursorPageResponse<Item> response = CursorPageResponse.of(slice, 1L, Item::id, Item::createdAt);

        assertThat(response.content()).isEqualTo(items);
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextAfter()).isNull();
        assertThat(response.size()).isEqualTo(5);
        assertThat(response.totalElements()).isEqualTo(1L);
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    @DisplayName("내용이 비어있으면 빈 응답을 반환한다")
    void of_empty() {
        SliceImpl<Item> slice = new SliceImpl<>(List.of(), Pageable.ofSize(5), false);

        CursorPageResponse<Item> response = CursorPageResponse.of(slice, 0L, Item::id, Item::createdAt);

        assertThat(response.content()).isEmpty();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextAfter()).isNull();
        assertThat(response.totalElements()).isZero();
        assertThat(response.hasNext()).isFalse();
    }
}
