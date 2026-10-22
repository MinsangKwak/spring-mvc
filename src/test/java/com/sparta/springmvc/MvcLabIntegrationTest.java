package com.sparta.springmvc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MvcLabIntegrationTest {
    @Autowired MockMvc mvc;

    @ParameterizedTest
    @CsvSource({"GET,/api/get", "POST,/api/post", "PUT,/api/put", "DELETE,/api/delete"})
    void methodRoutes(String method, String path) throws Exception {
        mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method), path))
                .andExpect(status().isOk()).andExpect(jsonPath("$.method").value(method));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/hello/request/form/param", "/hello/request/form/param/model"})
    void queryBinding(String path) throws Exception {
        assertBinding(get(path).param("name", "민상").param("age", "33"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/hello/request/form/param", "/hello/request/form/model"})
    void formBinding(String path) throws Exception {
        assertBinding(post(path).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "민상").param("age", "33"));
    }

    @Test
    void pathBinding() throws Exception {
        assertBinding(get("/hello/request/star/{name}/age/{age}", "민상", 33));
    }

    @Test
    void jsonBinding() throws Exception {
        assertBinding(post("/hello/request/form/json").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"민상\",\"age\":33}"));
    }

    private void assertBinding(MockHttpServletRequestBuilder request) throws Exception {
        mvc.perform(request).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("민상"))
                .andExpect(jsonPath("$.age").value(33))
                .andExpect(jsonPath("$.binding").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"name\":\" \",\"age\":33}", "{\"name\":\"민상\",\"age\":-1}", "{\"name\":\"민상\",\"age\":151}", "{\"name\":\"민상\",\"age\":null}"})
    void invalidJsonValues(String body) throws Exception {
        mvc.perform(post("/hello/request/form/json").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors", not(empty())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"name\":\"민상\",\"age\":\"33\"}", "{\"name\":\"민상\",\"age\":1.5}", "{\"name\":\"민상\",\"age\":33,\"unknown\":true}"})
    void invalidJsonFormat(String body) throws Exception {
        mvc.perform(post("/hello/request/form/json").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 150})
    void validAgeBoundaries(int age) throws Exception {
        mvc.perform(post("/hello/request/form/json").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"민상\",\"age\":" + age + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.age").value(age));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/hello/request/form/param", "/hello/request/form/param/model"})
    void queryValidation(String path) throws Exception {
        mvc.perform(get(path).param("name", " ").param("age", "151"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        mvc.perform(get(path).param("name", "민상").param("age", "abc"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].field").value("age"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/hello/request/form/param", "/hello/request/form/model"})
    void formValidation(String path) throws Exception {
        mvc.perform(post(path).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", " ").param("age", "-1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors", not(empty())));
    }

    @Test
    void pathRangeValidation() throws Exception {
        mvc.perform(get("/hello/request/star/name/age/151"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].field").value("age"));
    }

    @Test
    void missingParameter() throws Exception {
        mvc.perform(get("/hello/request/form/param").param("name", "민상"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].field").value("age"));
    }

    @Test
    void longName() throws Exception {
        mvc.perform(get("/hello/request/form/param").param("name", "가".repeat(41)).param("age", "33"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/response/json/string", "/response/rest/json/string"})
    void stringResponseIsPlainText(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("{\"name\":\"민상\",\"age\":33}"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/response/json/class", "/response/rest/json/class"})
    void objectResponseIsJson(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("민상"));
    }

    @Test
    void hostileInputRemainsJsonData() throws Exception {
        String name = "<img src=x onerror=alert(1)>";
        mvc.perform(get("/hello/request/form/param").param("name", name).param("age", "33"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    void wrongContentTypeAndMethod() throws Exception {
        mvc.perform(post("/hello/request/form/json").contentType(MediaType.TEXT_PLAIN).content("{}"))
                .andExpect(status().isUnsupportedMediaType()).andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
        mvc.perform(delete("/hello/request/form/json"))
                .andExpect(status().isMethodNotAllowed()).andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
        mvc.perform(get("/missing-page"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void htmlResponsesAndRedirects() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("같은 데이터,")));
        mvc.perform(get("/hello.html")).andExpect(status().isOk()).andExpect(content().string(containsString("정적 리소스")));
        mvc.perform(get("/html/templates")).andExpect(status().isOk()).andExpect(content().string(containsString("템플릿 렌더링")));
        mvc.perform(get("/html/dynamic")).andExpect(status().isOk()).andExpect(model().attribute("visits", greaterThan(0L)));
        mvc.perform(get("/html/redirect")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/hello.html"));
        mvc.perform(get("/static-hello")).andExpect(redirectedUrl("/hello.html"));
        mvc.perform(get("/hello/request/form/html")).andExpect(redirectedUrl("/#request-lab"));
    }

    @Test
    void openApiDescribesJsonAndFormRequests() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertThat(json.at("/info/title").asText()).contains("Spring MVC");
        assertThat(json.at("/paths/~1hello~1request~1form~1json/post/requestBody/content/application~1json").isMissingNode()).isFalse();
        assertThat(json.at("/paths/~1hello~1request~1form~1model/post/requestBody/content/application~1x-www-form-urlencoded").isMissingNode()).isFalse();
        assertThat(json.at("/paths/~1hello~1request~1form~1param/post/requestBody/content/application~1x-www-form-urlencoded").isMissingNode()).isFalse();
        assertThat(json.at("/paths/~1hello~1request~1form~1html").isMissingNode()).isTrue();
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk()).andExpect(content().string(containsString("Swagger UI")));
    }
}
