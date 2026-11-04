package com.sprint.deokhugam.common.util;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Slice;

class SliceUtilsTest {

    @Test
    @DisplayName("결과가 limit보다 많으면 마지막 1개를 제거하고 hasNext는 true")
    void toSlice_moreThanLimit() {
        // given
        List<Integer> result = List.of(1, 2, 3, 4);

        // when
        Slice<Integer> slice = SliceUtils.toSlice(result, 3);

        // then
        assertThat(slice.getContent()).containsExactly(1, 2, 3);
        assertThat(slice.hasNext()).isTrue();
        assertThat(slice.getSize()).isEqualTo(3);
    }

    @Test
    @DisplayName("결과가 limit과 같으면 그대로 반환하고 hasNext는 false")
    void toSlice_equalToLimit() {
        Slice<Integer> slice = SliceUtils.toSlice(List.of(1, 2, 3), 3);

        assertThat(slice.getContent()).containsExactly(1, 2, 3);
        assertThat(slice.hasNext()).isFalse();
    }

    @Test
    @DisplayName("결과가 limit보다 적으면 그대로 반환하고 hasNext는 false")
    void toSlice_lessThanLimit() {
        Slice<Integer> slice = SliceUtils.toSlice(List.of(1, 2), 3);

        assertThat(slice.getContent()).containsExactly(1, 2);
        assertThat(slice.hasNext()).isFalse();
    }

    @Test
    @DisplayName("결과가 비어있으면 빈 Slice를 반환하고 size는 limit")
    void toSlice_empty() {
        Slice<Integer> slice = SliceUtils.toSlice(List.of(), 5);

        assertThat(slice.getContent()).isEmpty();
        assertThat(slice.hasNext()).isFalse();
        assertThat(slice.getSize()).isEqualTo(5);
    }
}
