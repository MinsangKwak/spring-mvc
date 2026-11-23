package com.sparta.springmvc.html;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HTML 응답 비교 예제. 뷰 이름 반환(Thymeleaf 렌더링)과 redirect: 접두사(302)의 차이를 보여준다.
 * 방문 횟수는 이 인스턴스의 메모리 값이며 재시작하면 초기화되고 서버 간 공유되지 않는다.
 */
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
