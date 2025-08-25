package ru.practicum.ewm.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.service.compilation.CompilationService;

@Controller
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCompilationController {
    private final CompilationService service;

    @PostMapping
    public ResponseEntity<CompilationDto> create(@RequestBody @Valid NewCompilationDto dto) {
        log.info("Запрос на создание подборки событий: {}", dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> update(@PathVariable @Positive Long compId,
                                                 @RequestBody @Valid UpdateCompilationRequest request) {
        log.info("Запрос на обновление подборки событий id={}", request);
        return ResponseEntity.ok(service.update(compId, request));
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long compId) {
        log.info("Запрос на удаление подборки событий id={}", compId);
        service.delete(compId);
        return ResponseEntity.noContent().build();
    }
}
