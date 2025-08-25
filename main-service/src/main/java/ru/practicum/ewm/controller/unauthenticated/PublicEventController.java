package ru.practicum.ewm.controller.unauthenticated;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.SearchParamsPublic;
import ru.practicum.ewm.exception.custom.InvalidParameterException;
import ru.practicum.ewm.model.event.EventSort;
import ru.practicum.ewm.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.util.ParserUtils.parseDateTime;
import static ru.practicum.ewm.util.ParserUtils.parseEnum;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicEventController {
    private final EventService service;
    private final StatsClient statsClient;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getAll(@RequestParam(required = false) @Size(min = 1, max = 7000) String text,
                                                      @RequestParam(required = false) List<@Positive Long> categories,
                                                      @RequestParam(required = false) Boolean paid,
                                                      @RequestParam(required = false) String rangeStart,
                                                      @RequestParam(required = false) String rangeEnd,
                                                      @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                      @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                      @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                      @RequestParam(defaultValue = "10") @Positive int size,
                                                      HttpServletRequest request) {
        log.info("Запрос на получение событий: text={} | categories ={} | paid={} | rangeStart={} | rangeEnd={} | onlyAvailable={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable);
        EventSort eventSort = parseEnum(sort, EventSort.class);
        LocalDateTime start = parseDateTime(rangeStart);
        LocalDateTime end = parseDateTime(rangeEnd);
        if (start != null && end != null && !start.isBefore(end)) {
            log.warn("Попытка передать некорректный временной интервал");
            throw new InvalidParameterException("Дата начала периода должна быть раньше даты окончания периода");
        }

        SearchParamsPublic params = SearchParamsPublic.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(start)
                .rangeEnd(end)
                .available(onlyAvailable)
                .build();
        List<EventShortDto> response = service.getAll(params, eventSort, from, size);
        trackEndpointRequest(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getById(@PathVariable @Positive Long eventId,
                                                HttpServletRequest request) {
        log.info("Запрос на получение события id={}", eventId);
        EventFullDto response = service.getById(eventId);
        trackEndpointRequest(request);
        return ResponseEntity.ok(response);
    }

    private void trackEndpointRequest(HttpServletRequest request) {
        statsClient.create(EndpointHit.builder()
                .app("main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }
}
