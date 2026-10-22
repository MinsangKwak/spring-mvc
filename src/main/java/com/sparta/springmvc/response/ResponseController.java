package com.sparta.springmvc.response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/response")
@Tag(name = "03 · 응답 형식", description = "@Controller + @ResponseBody에서 문자열과 객체 응답을 비교합니다.")
public class ResponseController {
    @GetMapping(value = "/json/string", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    @Operation(summary = "JSON 형태 문자열 · text/plain")
    public String stringJson() {
        return "{\"name\":\"민상\",\"age\":33}";
    }

    @GetMapping(value = "/json/class", produces = "application/json")
    @ResponseBody
    @Operation(summary = "Java 객체 · application/json")
    public Star classJson() { return new Star("민상", 33); }
}
