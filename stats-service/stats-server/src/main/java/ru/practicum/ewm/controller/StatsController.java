package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Validated
@Slf4j
public class StatsController {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final StatsService service;

    @PostMapping("/hit")
    public ResponseEntity<Void> create(@RequestBody @Valid EndpointHit endpointHit) {
        log.info("Запрос на создание события: {}", endpointHit);
        service.create(endpointHit);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStats>> get(@RequestParam @DateTimeFormat(pattern = DATE_TIME_FORMAT) @NotNull LocalDateTime start,
                                               @RequestParam @DateTimeFormat(pattern = DATE_TIME_FORMAT) @NotNull LocalDateTime end,
                                               @RequestParam(required = false) List<String> uris,
                                               @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Запрос на получение статистики: start={} | end={} | uris={} | unique={}", start, end, uris, unique);
        List<ViewStats> response = service.get(start, end, uris, unique);
        return ResponseEntity.ok(response);
    }
}
