package com.sparta.springmvc;

import com.sparta.springmvc.error.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 2026-11-09 오류가 "어느 단계"에서 났는지에 따라 응답 모양이 달라지는 규칙을 고정한다. docs/ARCHITECTURE.md의 흐름 번호와 대응
@SpringBootTest
@AutoConfigureMockMvc
class ErrorResponseContractTest {
    @Autowired MockMvc mvc;

    private static final String JSON = "/hello/request/form/json";

    // 3단계(객체 만들기) 실패: Jackson이 "33"을 정수로 받지 않음 → 객체가 없으므로 필드 정보도 없다
    @Test
    void jsonTypeMismatchFailsBeforeObjectExists() throws Exception {
        mvc.perform(post(JSON).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"민상\",\"age\":\"33\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errors", empty()));
    }

    // 4단계(검증) 실패: 객체는 만들어졌고 @Max 제약에 걸림 → Star의 message가 그대로 필드 오류로 나간다
    @Test
    void jsonConstraintViolationCarriesFieldMessage() throws Exception {
        mvc.perform(post(JSON).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"민상\",\"age\":151}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("age"))
                .andExpect(jsonPath("$.errors[0].message").value("나이는 150 이하여야 합니다."));
    }

    // 같은 age=abc라도 읽는 애노테이션이 다르면 예외가 달라 code가 갈린다
    @Test
    void sameWrongAgeIsClassifiedByBindingStyle() throws Exception {
        // @RequestParam: 인자 변환 실패(MethodArgumentTypeMismatchException) → 요청 단계 오류
        mvc.perform(get("/hello/request/form/param").param("name", "민상").param("age", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errors[0].field").value("age"));
        // @ModelAttribute: 변환 실패가 BindingResult에 담겨 MethodArgumentNotValidException → 입력 단계 오류
        mvc.perform(get("/hello/request/form/param/model").param("name", "민상").param("age", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("age"))
                .andExpect(jsonPath("$.errors[0].message").value("입력 형식을 확인해 주세요."));
    }

    // 분류되지 않은 예외도 ApiError 형식을 유지하고 500으로 응답한다. 테스트 전용 컨트롤러로 예외를 일으킨다
    @Test
    void unexpectedExceptionKeepsApiErrorShape() throws Exception {
        MockMvc standalone = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new ApiExceptionHandler()).build();
        standalone.perform(get("/throw"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.errors", empty()));
    }

    @RestController
    public static class ThrowingController {
        @GetMapping("/throw")
        public String fail() { throw new IllegalStateException("테스트용 예외"); }
    }
}
