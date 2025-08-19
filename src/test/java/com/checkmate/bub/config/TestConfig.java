package com.checkmate.bub.config;

import com.checkmate.bub.ai.clova.ClovaClient;
import com.checkmate.bub.ai.clova.ClovaSpeechClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public ClovaClient clovaClient() {
        return mock(ClovaClient.class);
    }

    @Bean
    @Primary
    public ClovaSpeechClient clovaSpeechClient() {
        return mock(ClovaSpeechClient.class);
    }
}