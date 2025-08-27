package ru.practicum.ewm.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.SearchParamsAdmin;
import ru.practicum.ewm.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.exception.custom.InvalidParameterException;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.util.ParserUtils.parseDateTime;
import static ru.practicum.ewm.util.ParserUtils.parseEnum;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminEventController {
    private final EventService service;

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateByAdmin(@PathVariable @Positive Long eventId,
                                                      @RequestBody @Valid UpdateEventAdminRequest request) {
        log.info("Запрос на обновление администратором события id={}", eventId);
        return ResponseEntity.ok(service.updateByAdmin(eventId, request));
    }

    @GetMapping
    public ResponseEntity<List<EventFullDto>> getAllByAdmin(@RequestParam(required = false) List<@Positive Long> users,
                                                            @RequestParam(required = false) List<String> states,
                                                            @RequestParam(required = false) List<@Positive Long> categories,
                                                            @RequestParam(required = false) String rangeStart,
                                                            @RequestParam(required = false) String rangeEnd,
                                                            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                            @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Запрос на получение администратором событий: users={} | states={} | categories={} | rangeStart={} | rangeEnd={}",
                users, states, categories, rangeStart, rangeEnd);
        List<EventState> eventStates = parseEnum(states, EventState.class);
        LocalDateTime start = parseDateTime(rangeStart);
        LocalDateTime end = parseDateTime(rangeEnd);
        if (start != null && end != null && !start.isBefore(end)) {
            log.warn("Попытка передать некорректный временной интервал");
            throw new InvalidParameterException("Дата начала периода должна быть раньше даты окончания периода");
        }

        SearchParamsAdmin params = SearchParamsAdmin.builder()
                .users(users)
                .states(states != null ? eventStates : null)
                .categories(categories)
                .rangeStart(start)
                .rangeEnd(end)
                .build();
        return ResponseEntity.ok(service.getAllByAdmin(params, from, size));
    }
}
