package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.entity.Hit;
import ru.practicum.ewm.mapper.StatsMapper;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository repository;
    private final StatsMapper mapper;

    @Override
    @Transactional
    public void create(EndpointHit endpointHit) {
        Hit hit = repository.save(mapper.toEntity(endpointHit));
        log.info("Сохранена информация о запросе к эндпоинту - {}", hit);
    }

    @Override
    public List<ViewStats> get(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<ViewStats> stats = repository.find(start, end, uris, unique);
        log.info("Возвращена статистика по посещениям, размер списка - {}", stats.size());
        return stats;
    }
}
