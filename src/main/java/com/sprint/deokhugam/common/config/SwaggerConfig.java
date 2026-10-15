package com.sprint.deokhugam.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
            .info(new Info()
                .title("덕후감 API")
                .description("도서 이미지 OCR 기반 ISBN 매칭 도서 리뷰 서비스")
                .version("v1"));
    }
}
