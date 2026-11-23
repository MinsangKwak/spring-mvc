package com.sparta.springmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.exc.StreamConstraintsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "server.forward-headers-strategy=framework")
@AutoConfigureMockMvc
class SecurityContractTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void applicationResponsesSetSecurityAndCacheHeaders() throws Exception {
        mvc.perform(get("/").header(HttpHeaders.ORIGIN, "https://attacker.example"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Cache-Control", "no-store, max-age=0"))
                .andExpect(header().string("Content-Security-Policy", containsString("frame-ancestors 'none'")))
                .andExpect(header().string("Content-Security-Policy", not(containsString("unsafe-inline"))))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .andExpect(header().doesNotExist("Strict-Transport-Security"));
        mvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", "no-store, max-age=0"));
    }

    @Test
    void forwardedHttpsRequestsSetHstsAfterProxyNormalization() throws Exception {
        mvc.perform(get("/")
                        .header("X-Forwarded-Proto", "https")
                        .header("X-Forwarded-Host", "lab.example"))
                .andExpect(status().isOk())
                .andExpect(header().string("Strict-Transport-Security", "max-age=31536000; includeSubDomains"));
    }

    @Test
    void javaSwaggerKeepsNamedUrlAndLegacyRedirect() throws Exception {
        mvc.perform(get("/java-swagger"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"swagger-ui\"")))
                .andExpect(content().string(containsString("/js/java-swagger.js")))
                .andExpect(header().string("Content-Security-Policy", containsString("style-src 'self' 'unsafe-inline'")))
                .andExpect(header().string("Content-Security-Policy", not(containsString("script-src 'self' 'unsafe-inline'"))));
        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/java-swagger"));
    }

    @Test
    void oversizedJsonStringIsRejectedBeforeBeanValidation() throws Exception {
        String body = objectMapper.writeValueAsString(new TestInput("가".repeat(201), 33));
        mvc.perform(post("/hello/request/form/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void oversizedJsonBodyReturns413() throws Exception {
        String body = "{\"name\":\"" + "a".repeat(17 * 1024) + "\",\"age\":33}";
        mvc.perform(post("/hello/request/form/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(413))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("PAYLOAD_TOO_LARGE"));
    }

    @Test
    void jsonParserLimitsDepthAndNumberLength() {
        String nested = "[".repeat(21) + "0" + "]".repeat(21);
        assertThatThrownBy(() -> objectMapper.readTree(nested))
                .isInstanceOf(StreamConstraintsException.class);
        assertThatThrownBy(() -> objectMapper.readTree("123456789012345678901"))
                .isInstanceOf(StreamConstraintsException.class);
    }

    @Test
    void forwardedHostIsNotPublishedInOpenApi() throws Exception {
        String body = mvc.perform(get("/v3/api-docs")
                        .header("Forwarded", "host=attacker.example;proto=https")
                        .header("X-Forwarded-Host", "attacker.example"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var json = objectMapper.readTree(body);
        assertThat(json.at("/servers/0/url").asText()).isEqualTo("/");
        assertThat(body).doesNotContain("attacker.example");
    }

    private record TestInput(String name, int age) {}
}
