package com.mazicfilip.liveevents.controller;

import com.mazicfilip.liveevents.dto.EventStatusRequest;
import com.mazicfilip.liveevents.service.EventService;
import com.mazicfilip.liveevents.enums.EventStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventStatusController {

    private final EventService eventService;

    @PostMapping("/status")
    public ResponseEntity<String> updateStatus(@RequestBody EventStatusRequest request) {
        try {
            EventStatus status = EventStatus.fromString(request.status());
            eventService.updateEventStatus(request.eventId(), status);
            return ResponseEntity.ok("Status updated");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
