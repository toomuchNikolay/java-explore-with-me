package ru.practicum.ewm.controller.admin;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.reaction.ReactionDto;
import ru.practicum.ewm.service.reaction.ReactionService;

import java.util.List;

@RestController
@RequestMapping("/admin/reactions")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminReactionController {
    private final ReactionService service;

    @GetMapping
    public ResponseEntity<List<ReactionDto>> getByAdmin(@RequestParam(required = false) @Positive Long userId,
                                                        @RequestParam(required = false) @Positive Long eventId,
                                                        @RequestParam(required = false) Boolean isLike,
                                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                        @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Запрос на получение администратором оценок: userId={} | eventId={} | isLike={}", userId, eventId, isLike);
        return ResponseEntity.ok(service.getReactionsByAdmin(userId, eventId, isLike, from, size));
    }

    @DeleteMapping("/{reactionId}")
    public ResponseEntity<Void> deleteByAdmin(@PathVariable @Positive Long reactionId) {
        log.info("Запрос на удаление администратором оценки: id={}", reactionId);
        service.deleteByAdmin(reactionId);
        return ResponseEntity.noContent().build();
    }
}
