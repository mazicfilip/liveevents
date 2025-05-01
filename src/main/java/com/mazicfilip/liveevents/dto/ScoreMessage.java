package com.mazicfilip.liveevents.dto;

import java.time.Instant;

public record ScoreMessage(String eventId,
                           String currentScore,
                           Instant timestamp) {
}
