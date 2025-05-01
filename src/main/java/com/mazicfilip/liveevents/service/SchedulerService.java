package com.mazicfilip.liveevents.service;

import com.mazicfilip.liveevents.dto.ScoreApiResponse;
import com.mazicfilip.liveevents.dto.ScoreMessage;
import com.mazicfilip.liveevents.enums.EventStatus;
import com.mazicfilip.liveevents.integration.ExternalScoreClient;
import com.mazicfilip.liveevents.integration.KafkaScoreProducer;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;


@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final TaskScheduler taskScheduler;
    private final ExternalScoreClient scoreClient;
    private final KafkaScoreProducer producer;

    @Getter
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    public void onStatusChange(String eventId, EventStatus status) {
        switch (status) {
            case LIVE     -> startTask(eventId);
            case NOT_LIVE -> stopTask(eventId);
        }
    }

    private void startTask(String eventId) {
        tasks.computeIfAbsent(eventId, id -> {
            log.info("Scheduling job for event {}", id);
            return taskScheduler.scheduleAtFixedRate(
                    () -> fetchAndPublish(id),
                    Duration.ofSeconds(10));
        });
    }

    private void stopTask(String eventId) {
        ScheduledFuture<?> future = tasks.remove(eventId);
        if (future != null) {
            future.cancel(true);
            log.info("Stopped job for event {}", eventId);
        }
    }

    private void fetchAndPublish(String eventId) {
        try {
            ScoreApiResponse resp = scoreClient.getScore(eventId);
            producer.send(new ScoreMessage(
                    resp.eventId(), resp.currentScore(), Instant.now()));
        } catch (Exception ex) {
            log.error("Error during fetch/publish for {}", eventId, ex);
        }
    }

    @PreDestroy
    public void shutdown() {
        tasks.values().forEach(f -> f.cancel(true));
    }
}
