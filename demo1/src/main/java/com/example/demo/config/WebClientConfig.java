package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient postsMicroserviceClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081") // URL-ul microserviciului de postări și comentarii
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
                .build();
    }
}

