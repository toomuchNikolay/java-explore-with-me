package ru.practicum.ewm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.entity.Hit;
import ru.practicum.ewm.mapper.StatsMapper;
import ru.practicum.ewm.repository.StatsRepository;
import ru.practicum.ewm.service.StatsServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {
    @Mock
    private StatsRepository repository;

    @Mock
    private StatsMapper mapper;

    @InjectMocks
    private StatsServiceImpl service;

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
    void create_whenInvokedWithValidEndpointHit_thenResponseStatusCreatedAndSavedHit() {
        Hit hit = Hit.builder()
                .id(1L)
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp())
                .build();
        when(mapper.toEntity(endpointHit)).thenReturn(hit);
        when(repository.save(any(Hit.class))).thenReturn(hit);

        service.create(endpointHit);

        verify(mapper, times(1)).toEntity(endpointHit);
        verify(repository, times(1)).save(hit);
    }

    @Test
    void get_whenInvokedWithValidParameters_thenResponseStatusOkWithViewStatsInBody() {
        LocalDateTime start = LocalDateTime.of(2025, 8, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 9, 1, 0, 0, 0);
        List<ViewStats> expectedStats = List.of(viewStats);
        when(service.get(start, end, null, false)).thenReturn(expectedStats);

        List<ViewStats> response = service.get(start, end, null, false);

        assertThat(response, notNullValue());
        assertThat(response.getFirst(), equalTo(viewStats));
        verify(repository, times(1)).find(any(), any(), any(), anyBoolean());
    }
}