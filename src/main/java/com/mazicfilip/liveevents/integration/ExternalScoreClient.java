package com.mazicfilip.liveevents.integration;


import com.mazicfilip.liveevents.dto.ScoreApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalScoreClient {

    @Value("${score-api.base-url}")
    private String baseUrl;

    private final WebClient.Builder builder;

    @Retryable(
            retryFor = { WebClientResponseException.class, IOException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000))
    public ScoreApiResponse getScore(String eventId) {
        return builder.baseUrl(baseUrl)
                .build()
                .get()
                .uri("/score/{id}", eventId)
                .retrieve()
                .bodyToMono(ScoreApiResponse.class)
                .block();
    }
}
