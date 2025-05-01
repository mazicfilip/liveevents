package com.mazicfilip.liveevents.service;

import com.mazicfilip.liveevents.enums.EventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EventServiceTest {

    private SchedulerService scheduler;
    private EventService     service;

    @BeforeEach
    void setUp() {
        scheduler = mock(SchedulerService.class);
        service   = new EventService(scheduler);
    }

    @Test
    void updateEventStatus_storesValue_andNotifiesScheduler() {
        service.updateEventStatus("match7", EventStatus.LIVE);

        assertThat(service.getEventMap())
                .containsEntry("match7", EventStatus.LIVE);

        ArgumentCaptor<EventStatus> capt = ArgumentCaptor.forClass(EventStatus.class);
        verify(scheduler).onStatusChange(eq("match7"), capt.capture());
        assertThat(capt.getValue()).isEqualTo(EventStatus.LIVE);
    }
}
