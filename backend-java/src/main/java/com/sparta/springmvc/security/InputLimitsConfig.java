package com.sparta.springmvc.security;

import com.fasterxml.jackson.core.StreamReadConstraints;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InputLimitsConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer streamReadConstraints() {
        StreamReadConstraints constraints = StreamReadConstraints.builder()
                .maxNestingDepth(20)
                .maxStringLength(200)
                .maxNumberLength(20)
                .build();
        return builder -> builder.postConfigurer(
                objectMapper -> objectMapper.getFactory().setStreamReadConstraints(constraints));
    }
}
