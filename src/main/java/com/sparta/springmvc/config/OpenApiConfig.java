package com.sparta.springmvc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI practiceApi() {
        return new OpenAPI().info(new Info()
                .title("Spring MVC · 요청과 응답 실습")
                .version("1.0")
                .description("구직용 학습 프로젝트입니다. Try it out으로 실제 서버에 요청하고 상태 코드, Content-Type, 응답 본문을 비교합니다. "
                        + "DB·인증·실제 CRUD는 구현 범위에 포함하지 않습니다. HTML 실습은 홈 화면에서 확인할 수 있습니다."));
    }
}
