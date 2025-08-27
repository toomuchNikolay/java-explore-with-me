package ru.practicum.ewm.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.exception.custom.ConflictException;
import ru.practicum.ewm.exception.custom.NotFoundException;
import ru.practicum.ewm.mapper.category.CategoryMapper;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.repository.category.CategoryRepository;
import ru.practicum.ewm.repository.event.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper mapper;

    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto dto) {
        validateNameAvailability(dto.getName());
        Category category = categoryRepository.save(mapper.toEntity(dto));
        log.info("Сохранена категория - {}", category);
        return mapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryDto dto) {
        Category category = findCategoryOrThrow(id);
        if (!category.getName().equalsIgnoreCase(dto.getName())) {
            validateNameAvailability(dto.getName());
            category.setName(dto.getName());
        }
        log.info("Обновлена категория - {}", category);
        return mapper.toDto(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = findCategoryOrThrow(id);
        if (eventRepository.existsByCategoryId(id)) {
            log.info("Попытка удалить использующуюся категорию");
            throw new ConflictException("Запрещено удалять категорию, использующуюся в событиях");
        }
        categoryRepository.delete(category);
        log.info("Удалена категория - {}", category);
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        List<Category> categories = categoryRepository.findAll(page).getContent();
        log.info("Возвращен список категорий в размере - {}", categories.size());
        return mapper.toDto(categories);
    }

    @Override
    public CategoryDto getById(Long id) {
        Category category = findCategoryOrThrow(id);
        log.info("Возвращена категория - {}", category);
        return mapper.toDto(category);
    }

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + id + " не найдена"));
    }

    private void validateNameAvailability(String name) {
        if (categoryRepository.existsByName(name)) {
            log.warn("Попытка создать уже имеющуюся категорию - {}", name);
            throw new ConflictException("Категория с указанным названием уже существует");
        }
    }
}
