package com.sparta.springmvc.response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/response/rest")
@Tag(name = "04 · RestController", description = "@RestController가 적용하는 @ResponseBody 동작을 비교합니다.")
public class ResponseRestController {
    @GetMapping(value = "/json/string", produces = "text/plain;charset=UTF-8")
    @Operation(summary = "RestController 문자열 · text/plain")
    public String stringJson() {
        return "{\"name\":\"민상\",\"age\":33}";
    }

    @GetMapping(value = "/json/class", produces = "application/json")
    @Operation(summary = "RestController 객체 · application/json")
    public Star classJson() { return new Star("민상", 33); }
}
