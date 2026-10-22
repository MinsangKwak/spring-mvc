package com.sparta.springmvc.response;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/response")
public class ResponseController {
    @GetMapping(value = "/json/string", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String stringJson() {
        return "{\"name\":\"민상\",\"age\":33}";
    }

    @GetMapping(value = "/json/class", produces = "application/json")
    @ResponseBody
    public Star classJson() { return new Star("민상", 33); }
}
