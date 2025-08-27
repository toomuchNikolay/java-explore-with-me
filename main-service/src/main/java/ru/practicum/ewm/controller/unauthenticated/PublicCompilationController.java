package ru.practicum.ewm.controller.unauthenticated;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.service.compilation.CompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PublicCompilationController {
    private final CompilationService service;

    @GetMapping
    public ResponseEntity<List<CompilationDto>> getAll(@RequestParam(required = false) Boolean pinned,
                                                       @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                       @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Запрос на получение событий: pinned={}", pinned);
        return ResponseEntity.ok(service.getAll(pinned, from, size));
    }

    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDto> getById(@PathVariable @Positive Long compId) {
        log.info("Запрос на получение события id={}", compId);
        return ResponseEntity.ok(service.getById(compId));
    }
}
