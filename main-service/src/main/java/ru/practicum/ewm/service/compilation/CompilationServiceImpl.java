package ru.practicum.ewm.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.exception.custom.NotFoundException;
import ru.practicum.ewm.mapper.compilation.CompilationMapper;
import ru.practicum.ewm.model.compilation.Compilation;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.compilation.CompilationRepository;
import ru.practicum.ewm.repository.event.EventRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper mapper;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        List<Event> events = eventRepository.findAllByIdIn(dto.getEvents());
        Compilation compilation = compilationRepository.save(mapper.toEntity(dto, events));
        log.info("Сохранена подборка событий - {}", compilation);
        return mapper.toDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = findCompilationOrThrow(compId);
        updateFields(compilation, request);
        log.info("Обновлена подборка событий - {}", compilation);
        return mapper.toDto(compilation);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        Compilation compilation = findCompilationOrThrow(compId);
        compilationRepository.delete(compilation);
        log.info("Удалена подборка событий - {}", compilation);
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        List<Compilation> compilations = pinned != null
                ? compilationRepository.findAllByPinned(pinned, page).getContent()
                : compilationRepository.findAll(page).getContent();
        log.info("Возвращен список подборок событий в размере - {}", compilations.size());
        return mapper.toDto(compilations);
    }

    @Override
    public CompilationDto getById(Long compId) {
        Compilation compilation = findCompilationOrThrow(compId);
        log.info("Возвращена подборка событий - {}", compilation);
        return mapper.toDto(compilation);
    }

    private Compilation findCompilationOrThrow(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id=" + compId + " не найдена"));
    }

    private void updateFields(Compilation entity, UpdateCompilationRequest dto) {
        Optional.ofNullable(dto.getTitle()).ifPresent(entity::setTitle);
        Optional.ofNullable(dto.getPinned()).ifPresent(entity::setPinned);
        Optional.ofNullable(dto.getEvents()).ifPresent(ids -> {
            List<Event> events = eventRepository.findAllByIdIn(dto.getEvents());
            entity.setEvents(events);
        });
    }
}
