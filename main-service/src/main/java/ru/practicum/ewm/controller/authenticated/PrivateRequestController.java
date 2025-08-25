package ru.practicum.ewm.controller.authenticated;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.service.request.RequestService;

import java.util.List;

@Controller
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateRequestController {
    private final RequestService service;

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> create(@PathVariable @Positive Long userId,
                                                          @RequestParam @Positive Long eventId) {
        log.info("Запрос на создание заявки на участие пользователя id={} в событии id={}", userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(userId, eventId));
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancel(@PathVariable @Positive Long userId,
                                                          @PathVariable @Positive Long requestId) {
        log.info("Запрос на отмену заявки id={}", requestId);
        return ResponseEntity.ok(service.cancel(userId, requestId));
    }

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getAllRequestByUser(@PathVariable @Positive Long userId) {
        log.info("Запрос на получение всех заявок пользователя id={}", userId);
        return ResponseEntity.ok(service.getAllRequestByUser(userId));
    }
}
