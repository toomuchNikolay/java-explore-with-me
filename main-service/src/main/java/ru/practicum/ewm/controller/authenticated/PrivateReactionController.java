package ru.practicum.ewm.controller.authenticated;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.reaction.ReactionDto;
import ru.practicum.ewm.service.reaction.ReactionService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.util.ParserUtils.parseDateTime;

@RestController
@RequestMapping("/users/{userId}/reactions")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PrivateReactionController {
    // Добавлен заголовок для тестирования добавления оценок событий
    // Причина - события создаются минимум через 2 часа от текущей даты, а оценки события создаются после даты события
    private static final String TEST_HEADER = "X-Test-Time";

    private final ReactionService service;

    @PostMapping
    public ResponseEntity<ReactionDto> create(@PathVariable @Positive Long userId,
                                              @RequestParam @Positive Long eventId,
                                              @RequestParam Boolean isLike,
                                              @RequestHeader(required = false, value = TEST_HEADER) String testTime) {
        log.info("Запрос на создание пользователем id={} оценки {} событию id={}", userId, isLike, eventId);
        LocalDateTime time = testTime != null
                ? parseDateTime(testTime)
                : LocalDateTime.now();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(userId, eventId, isLike, time));
    }

    @GetMapping
    public ResponseEntity<List<ReactionDto>> getReactionsByUser(@PathVariable @Positive Long userId) {
        log.info("Запрос на получение всех своих оценок пользователем id={}", userId);
        return ResponseEntity.ok(service.getReactionsByUser(userId));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<List<ReactionDto>> getReactionsByEvent(@PathVariable @Positive Long userId,
                                                                 @PathVariable @Positive Long eventId) {
        log.info("Запрос на получение всех оценок пользователем id={} на событие id={}", userId, eventId);
        return ResponseEntity.ok(service.getReactionsByEvent(userId, eventId));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteByUser(@PathVariable @Positive Long userId,
                                             @PathVariable @Positive Long eventId) {
        log.info("Запрос на удаление оценки пользователем id={} на событие id={}", userId, eventId);
        service.deleteByUser(userId, eventId);
        return ResponseEntity.noContent().build();
    }
}
