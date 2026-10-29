package com.sprint.deokhugam.comment;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sprint.deokhugam.comment.domain.ProfanityWord;

class ProfanityWordTest {

    @Test
    @DisplayName("비속어는 ***로 치환된다")
    void mask_replacesProfanity() {
        assertThat(ProfanityWord.mask("이 시발 리뷰 뭐야")).isEqualTo("이 *** 리뷰 뭐야");
    }

    @Test
    @DisplayName("여러 개의 비속어가 모두 치환된다")
    void mask_replacesMultiple() {
        assertThat(ProfanityWord.mask("시발 개새끼 병신")).isEqualTo("*** *** ***");
    }

    @Test
    @DisplayName("영문 비속어는 대소문자를 구분하지 않는다")
    void mask_ignoreCase() {
        assertThat(ProfanityWord.mask("What the FuCk")).isEqualTo("What the ***");
    }

    @Test
    @DisplayName("비속어가 없으면 원문 그대로 반환한다")
    void mask_noProfanity() {
        assertThat(ProfanityWord.mask("좋은 리뷰네요!")).isEqualTo("좋은 리뷰네요!");
    }

    @Test
    @DisplayName("null/빈 문자열은 그대로 반환한다")
    void mask_nullOrEmpty() {
        assertThat(ProfanityWord.mask(null)).isNull();
        assertThat(ProfanityWord.mask("")).isEmpty();
    }

    @Test
    @DisplayName("치환 문자열의 특수문자($, \\)가 있어도 안전하다")
    void mask_specialCharacters() {
        assertThat(ProfanityWord.mask("$1 시발 \\")).isEqualTo("$1 *** \\");
    }
}
