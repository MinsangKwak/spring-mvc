package com.sparta.springmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.springmvc.response.Star;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class JacksonTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void objectToJsonPreservesValues() throws Exception {
        var json = mapper.readTree(mapper.writeValueAsString(new Star("민상", 33)));
        assertThat(json.get("name").asText()).isEqualTo("민상");
        assertThat(json.get("age").asInt()).isEqualTo(33);
    }

    @Test
    void jsonToObjectPreservesValues() throws Exception {
        Star star = mapper.readValue("{\"name\":\"민상\",\"age\":33}", Star.class);
        assertThat(star.getName()).isEqualTo("민상");
        assertThat(star.getAge()).isEqualTo(33);
    }
}
