package com.sparta.springmvc.response;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/response/rest")
public class ResponseRestController {
    @GetMapping(value = "/json/string", produces = "text/plain;charset=UTF-8")
    public String stringJson() {
        return "{\"name\":\"민상\",\"age\":33}";
    }

    @GetMapping(value = "/json/class", produces = "application/json")
    public Star classJson() { return new Star("민상", 33); }
}
