package ru.practicum.ewm.controller.unauthenticated;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.reaction.RatingEventDto;
import ru.practicum.ewm.dto.reaction.RatingUserDto;
import ru.practicum.ewm.model.reaction.RatingPeriod;
import ru.practicum.ewm.service.reaction.ReactionService;

import java.util.List;

import static ru.practicum.ewm.util.ParserUtils.parseEnum;

@RestController
@RequestMapping("/reactions")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicReactionController {
    private final ReactionService service;

    @GetMapping("/events")
    public ResponseEntity<List<RatingEventDto>> getPopularEvents(@RequestParam(defaultValue = "5") @Positive int limit,
                                                                 @RequestParam(defaultValue = "week") String period) {
        log.info("Запрос на получение {} популярных событий за период {}", limit, period);
        RatingPeriod ratingPeriod = parseEnum(period, RatingPeriod.class);
        return ResponseEntity.ok(service.getPopularEvents(limit, ratingPeriod));
    }

    @GetMapping("/users")
    public ResponseEntity<List<RatingUserDto>> getPopularUsers(@RequestParam(defaultValue = "5") @Positive int limit,
                                                               @RequestParam(defaultValue = "week") String period) {
        log.info("Запрос на получение {} популярных пользователей за период {}", limit, period);
        RatingPeriod ratingPeriod = parseEnum(period, RatingPeriod.class);
        return ResponseEntity.ok(service.getPopularUsers(limit, ratingPeriod));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<RatingEventDto> getEventRating(@PathVariable @Positive Long eventId) {
        log.info("Запрос на получение рейтинга события id={}", eventId);
        return ResponseEntity.ok(service.getEventRating(eventId));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<RatingUserDto> getUserRating(@PathVariable @Positive Long userId) {
        log.info("Запрос на получение рейтинга пользователя id={}", userId);
        return ResponseEntity.ok(service.getUserRating(userId));
    }
}
