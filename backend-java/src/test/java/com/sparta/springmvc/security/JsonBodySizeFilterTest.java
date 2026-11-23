package com.sparta.springmvc.security;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.assertj.core.api.Assertions.assertThat;

class JsonBodySizeFilterTest {
    private final JsonBodySizeFilter filter = new JsonBodySizeFilter();

    @Test
    void rejectsOversizedChunkedBodyWithoutContentLength() throws Exception {
        byte[] body = ("{\"name\":\"" + "a".repeat(JsonBodySizeFilter.MAX_JSON_BYTES) + "\",\"age\":33}")
                .getBytes(StandardCharsets.UTF_8);
        UnknownLengthRequest request = request(body);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean continued = new AtomicBoolean();

        filter.doFilter(request, response, (nextRequest, nextResponse) -> continued.set(true));

        assertThat(continued).isFalse();
        assertThat(response.getStatus()).isEqualTo(413);
        assertThat(response.getContentAsString()).contains("PAYLOAD_TOO_LARGE");
    }

    @Test
    void cachesAllowedChunkedBodyForMvc() throws Exception {
        byte[] body = "{\"name\":\"민상\",\"age\":33}".getBytes(StandardCharsets.UTF_8);
        UnknownLengthRequest request = request(body);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<byte[]> forwarded = new AtomicReference<>();

        filter.doFilter(request, response,
                (nextRequest, nextResponse) -> forwarded.set(nextRequest.getInputStream().readAllBytes()));

        assertThat(forwarded.get()).isEqualTo(body);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    private UnknownLengthRequest request(byte[] body) {
        UnknownLengthRequest request = new UnknownLengthRequest();
        request.setMethod("POST");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.addHeader("Transfer-Encoding", "chunked");
        request.setContent(body);
        return request;
    }

    private static final class UnknownLengthRequest extends MockHttpServletRequest {
        @Override
        public int getContentLength() {
            return -1;
        }

        @Override
        public long getContentLengthLong() {
            return -1L;
        }
    }
}
