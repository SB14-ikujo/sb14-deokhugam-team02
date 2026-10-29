package com.sprint.deokhugam.comment.domain;

import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProfanityWord {
    SIBAL("시발"),
    SSIBAL("씨발"),
    GAESAEKKI("개새끼"),
    BYEONGSIN("병신"),
    FUCK("fuck"),
    SHIT("shit");

    private static final String MASK = "***";

    // enum 상수가 모두 만들어진 뒤 한 번만 컴파일
    private static final Pattern PATTERN = Pattern.compile(
        Arrays.stream(values())
            .map(ProfanityWord::getWord)
            .sorted(Comparator.comparingInt(String::length).reversed())   // 긴 단어 우선 매칭
            .map(Pattern::quote)
            .reduce((a, b) -> a + "|" + b)
            .orElseThrow(),
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private final String word;

    // 비속어를 "***"로 치환한 문자열을 반환
    public static String mask(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        // 치환 문자열의 특수문자($, \)가 해석되지 않도록 quoteReplacement 사용
        return PATTERN.matcher(content).replaceAll(Matcher.quoteReplacement(MASK));
    }
}
