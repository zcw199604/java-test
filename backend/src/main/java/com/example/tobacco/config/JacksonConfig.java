package com.example.tobacco.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.featuresToEnable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .postConfigurer(objectMapper -> objectMapper.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true)));
    }
}
