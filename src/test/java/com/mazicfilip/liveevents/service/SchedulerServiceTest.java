package com.mazicfilip.liveevents.service;

import com.mazicfilip.liveevents.dto.ScoreApiResponse;
import com.mazicfilip.liveevents.enums.EventStatus;
import com.mazicfilip.liveevents.integration.ExternalScoreClient;
import com.mazicfilip.liveevents.integration.KafkaScoreProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SchedulerServiceTest {

    @Mock
    TaskScheduler taskScheduler;
    @Mock
    ExternalScoreClient scoreClient;
    @Mock
    KafkaScoreProducer producer;
    @Mock
    ScheduledFuture future;

    @Captor
    ArgumentCaptor<Runnable> runnableCaptor;

    SchedulerService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any(Duration.class)))
                .thenReturn(future);
        service = new SchedulerService(taskScheduler, scoreClient, producer);
    }


    @Test
    void liveStatus_schedulesJob_andPublishesMessage() {
        when(scoreClient.getScore("match1"))
                .thenReturn(new ScoreApiResponse("match1", "1:1"));

        service.onStatusChange("match1", EventStatus.LIVE);

        verify(taskScheduler).scheduleAtFixedRate(runnableCaptor.capture(),
                eq(Duration.ofSeconds(10)));

        runnableCaptor.getValue().run();

        verify(producer).send(argThat(msg ->
                msg.eventId().equals("match1") &&
                        msg.currentScore().equals("1:1")));

        service.onStatusChange("match1", EventStatus.LIVE);
        verifyNoMoreInteractions(taskScheduler);
    }


    @Test
    void notLiveStatus_cancelsScheduledTask() {
        service.onStatusChange("gameX", EventStatus.LIVE);
        service.onStatusChange("gameX", EventStatus.NOT_LIVE);

        verify(future).cancel(true);
        assertThat(service.getTasks()).isEmpty();
    }
}
