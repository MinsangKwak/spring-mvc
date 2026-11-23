package com.sparta.springmvc.request;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 요청 바인딩 실습의 입력 DTO. 폼·쿼리는 {@code @ModelAttribute}, JSON은 {@code @RequestBody}로 이 객체에 담긴다.
 * <p>
 * 제약(1~40자 이름, 0~150 정수 나이)과 오류 문구의 원본이다. 같은 제약이 {@link RequestController}의
 * 개별 인자 애노테이션에도 있으므로 범위를 바꿀 때는 두 곳과 문서·테스트를 함께 바꾼다.
 * 도메인 모델이나 DB 엔티티가 아니다.
 */
public class Star {
    @NotBlank(message = "이름을 입력해 주세요.")
    @Size(max = 40, message = "이름은 40자 이내로 입력해 주세요.")
    @Schema(description = "이름 · 1~40자", example = "민상")
    private String name;
    @NotNull(message = "나이를 입력해 주세요.")
    @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
    @Max(value = 150, message = "나이는 150 이하여야 합니다.")
    @Schema(description = "나이 · 0~150 정수", example = "33", type = "integer")
    private Integer age;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
