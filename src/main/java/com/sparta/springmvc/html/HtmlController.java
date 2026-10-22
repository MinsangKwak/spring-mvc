package com.sparta.springmvc.html;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HtmlController {
    private final AtomicLong visitCount = new AtomicLong();

    @GetMapping("/")
    public String home() { return "index"; }

    @GetMapping({"/static-hello", "/html/redirect"})
    public String staticHello() { return "redirect:/hello.html"; }

    @GetMapping("/html/templates")
    public String template() { return "hello"; }

    @GetMapping("/html/dynamic")
    public String dynamic(Model model) {
        model.addAttribute("visits", visitCount.incrementAndGet());
        return "hello-visit";
    }
}
