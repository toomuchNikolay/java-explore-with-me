package ru.practicum.ewm.controller.authenticated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.service.event.EventService;
import ru.practicum.ewm.service.request.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateEventController {
    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    public ResponseEntity<EventFullDto> create(@PathVariable @Positive Long userId,
                                               @RequestBody @Valid NewEventDto newDto) {
        log.info("Запрос на создание события: {}", newDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(userId, newDto));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateByUser(@PathVariable @Positive Long userId,
                                                     @PathVariable @Positive Long eventId,
                                                     @RequestBody @Valid UpdateEventUserRequest request) {
        log.info("Запрос на обновление пользователем id={} события id={}", userId, eventId);
        return ResponseEntity.ok(eventService.updateByUser(userId, eventId, request));
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateStatusRequest(@PathVariable @Positive Long userId,
                                                                              @PathVariable @Positive Long eventId,
                                                                              @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        log.info("Запрос на изменение статусов заявок на участие в событии id={}", eventId);
        return ResponseEntity.ok(requestService.updateStatusRequest(userId, eventId, request));
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getAllByUser(@PathVariable @Positive Long userId,
                                                            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                            @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Запрос на получение событий пользователя: id={}", userId);
        return ResponseEntity.ok(eventService.getAllByUser(userId, from, size));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getByUser(@PathVariable @Positive Long userId,
                                                  @PathVariable @Positive Long eventId) {
        log.info("Запрос на получение события id={} пользователем id={}", eventId, userId);
        return ResponseEntity.ok(eventService.getByUser(userId, eventId));
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getALlRequestByEvent(@PathVariable @Positive Long userId,
                                                                              @PathVariable @Positive Long eventId) {
        log.info("Запрос на получение заявок на участие в событии id={}", eventId);
        return ResponseEntity.ok(requestService.getALlRequestByEvent(userId, eventId));
    }
}
