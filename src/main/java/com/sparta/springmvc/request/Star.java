package com.sparta.springmvc.request;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

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
