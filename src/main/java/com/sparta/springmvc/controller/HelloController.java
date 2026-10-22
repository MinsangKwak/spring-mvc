package com.sparta.springmvc.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HelloController {
    public record MethodResponse(String method, String message) {}

    @GetMapping("/hello")
    public String hello() { return "Hello, Spring MVC!"; }

    @GetMapping("/get")
    public MethodResponse get() {
        return new MethodResponse("GET", "조회 요청을 확인했습니다. 데이터는 저장하지 않습니다.");
    }

    @PostMapping("/post")
    public MethodResponse post() {
        return new MethodResponse("POST", "생성 메서드 실습입니다. 실제 리소스를 생성하지 않습니다.");
    }

    @PutMapping("/put")
    public MethodResponse put() {
        return new MethodResponse("PUT", "수정 메서드 실습입니다. 실제 리소스를 수정하지 않습니다.");
    }

    @DeleteMapping("/delete")
    public MethodResponse delete() {
        return new MethodResponse("DELETE", "삭제 메서드 실습입니다. 실제 리소스를 삭제하지 않습니다.");
    }
}
