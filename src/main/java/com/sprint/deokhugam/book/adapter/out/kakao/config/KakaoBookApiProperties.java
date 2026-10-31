package com.sprint.deokhugam.book.adapter.out.kakao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/*
 * Kakao Rest Api 설정값 관리하는 클래스
 * - application.yaml에 설정한 kakao.book 값을 Java 객체로 받아온다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "kakao.book")
public class KakaoBookApiProperties {
    // 카카오 API 기본 주소
    private String baseUrl;

    // 카카오 REST API 키
    private String restApiKey;
}
