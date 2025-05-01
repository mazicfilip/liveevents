package com.mazicfilip.liveevents.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mazicfilip.liveevents.dto.EventStatusRequest;
import com.mazicfilip.liveevents.service.EventService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventStatusController.class)
public class EventStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void updateStatus_live_returnsOk() throws Exception {
        EventStatusRequest req = new EventStatusRequest("game42", "live");

        mockMvc.perform(post("/events/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Mockito.verify(eventService)
                .updateEventStatus("game42",
                        com.mazicfilip.liveevents.enums.EventStatus.LIVE);
    }
}
