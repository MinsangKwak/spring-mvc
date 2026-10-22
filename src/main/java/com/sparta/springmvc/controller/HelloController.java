package com.sparta.springmvc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "01 · HTTP 메서드", description = "HTTP 메서드별 라우팅을 비교합니다. 데이터 저장과 실제 CRUD는 수행하지 않습니다.")
public class HelloController {
    public record MethodResponse(String method, String message) {}

    @GetMapping("/hello")
    @Operation(summary = "문자열 응답 확인")
    public String hello() { return "Hello, Spring MVC!"; }

    @GetMapping("/get")
    @Operation(summary = "GET 요청 확인")
    public MethodResponse get() {
        return new MethodResponse("GET", "조회 요청을 확인했습니다. 데이터는 저장하지 않습니다.");
    }

    @PostMapping("/post")
    @Operation(summary = "POST 요청 확인")
    public MethodResponse post() {
        return new MethodResponse("POST", "생성 메서드 실습입니다. 실제 리소스를 생성하지 않습니다.");
    }

    @PutMapping("/put")
    @Operation(summary = "PUT 요청 확인")
    public MethodResponse put() {
        return new MethodResponse("PUT", "수정 메서드 실습입니다. 실제 리소스를 수정하지 않습니다.");
    }

    @DeleteMapping("/delete")
    @Operation(summary = "DELETE 요청 확인")
    public MethodResponse delete() {
        return new MethodResponse("DELETE", "삭제 메서드 실습입니다. 실제 리소스를 삭제하지 않습니다.");
    }
}
