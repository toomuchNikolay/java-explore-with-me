package ru.practicum.ewm.service;

import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void create(EndpointHit endpointHit);

    List<ViewStats> get(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
