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
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.service.category.CategoryService;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCategoryController {
    private final CategoryService service;

    @PostMapping
    public ResponseEntity<CategoryDto> create(@RequestBody @Valid NewCategoryDto newDto) {
        log.info("Запрос на создание категории: {}", newDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(newDto));
    }

    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> update(@PathVariable @Positive Long catId,
                                              @RequestBody @Valid CategoryDto updateDto) {
        log.info("Запрос на обновление категории id={}", catId);
        return ResponseEntity.ok(service.update(catId, updateDto));
    }

    @DeleteMapping("/{catId}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long catId) {
        log.info("Запрос на удаление категории id={}", catId);
        service.delete(catId);
        return ResponseEntity.noContent().build();
    }
}
