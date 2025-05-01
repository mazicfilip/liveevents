package com.mazicfilip.liveevents.service;

import com.mazicfilip.liveevents.enums.EventStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    @Getter
    private final Map<String, EventStatus> eventMap = new ConcurrentHashMap<>();
    private final SchedulerService scheduler;

    public void updateEventStatus(String id, EventStatus status) {
        eventMap.put(id, status);
        scheduler.onStatusChange(id, status);
        log.info("Updated {} → {}", id, status);
    }
}
