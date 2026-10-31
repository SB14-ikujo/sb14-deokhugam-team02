package com.sprint.deokhugam.book.adapter.out.kakao.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KakaoBookApiProperties.class)
public class KakaoBookApiConfig {
    @Bean
    public RestClient kakaoBookRestClient(
        KakaoBookApiProperties properties
    ) {
        return RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.getRestApiKey())
            .build();
    }
}
