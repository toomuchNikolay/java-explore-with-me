package ru.practicum.ewm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.ewm.controller.StatsController;
import ru.practicum.ewm.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
class StatsControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StatsService service;

    @Autowired
    private MockMvc mvc;

    private EndpointHit endpointHit;
    private ViewStats viewStats;

    @BeforeEach
    void setUp() {
        endpointHit = EndpointHit.builder()
                .app("main-service")
                .uri("/users/1")
                .ip("123.1.2.3")
                .timestamp(LocalDateTime.of(2025, 8, 18, 10, 0, 0))
                .build();
        viewStats = ViewStats.builder()
                .app("main-service")
                .uri("/users")
                .hits(1L)
                .build();
    }

    @Test
    @SneakyThrows
    void create() {
        doNothing().when(service).create(endpointHit);

        mvc.perform(post("/hit")
                .content(mapper.writeValueAsString(endpointHit))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @SneakyThrows
    void get() {
        when(service.get(LocalDateTime.of(2025, 8, 1, 0, 0, 0),
                LocalDateTime.of(2025, 9, 1, 0, 0, 0),
                null,
                false)).thenReturn(List.of(viewStats));

        mvc.perform(MockMvcRequestBuilders.get("/stats")
                        .param("start", "2025-08-01 00:00:00")
                        .param("end", "2025-09-01 00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app", is(viewStats.getApp())))
                .andExpect(jsonPath("$[0].uri", is(viewStats.getUri())))
                .andExpect(jsonPath("$[0].hits", is(viewStats.getHits()), Long.class));
    }
}