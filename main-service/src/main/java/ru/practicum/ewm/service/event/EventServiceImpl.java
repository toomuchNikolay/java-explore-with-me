package ru.practicum.ewm.service.event;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.ViewStats;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.exception.custom.AccessDeniedException;
import ru.practicum.ewm.exception.custom.ConflictException;
import ru.practicum.ewm.exception.custom.NotFoundException;
import ru.practicum.ewm.mapper.event.EventMapper;
import ru.practicum.ewm.model.event.*;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.category.CategoryRepository;
import ru.practicum.ewm.repository.event.EventRepository;
import ru.practicum.ewm.repository.request.RequestRepository;
import ru.practicum.ewm.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.ewm.model.event.EventState.*;
import static ru.practicum.ewm.model.event.StateAction.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final EventMapper mapper;
    private final StatsClient client;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        User user = findUser(userId);
        Category category = findCategory(dto.getCategory());
        Event event = eventRepository.save(mapper.toEntity(dto, category, user));
        log.info("Добавлено событие - {}", event);
        return mapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = findEvent(eventId);

        if (request.getStateAction() != null && request.getStateAction() == PUBLISH_EVENT && event.getState() != PENDING) {
            log.warn("Попытка опубликовать не в состоянии ожидания событие id={}", eventId);
            throw new ConflictException("Публиковать можно только события в состоянии ожидания");
        }
        if (request.getStateAction() != null && request.getStateAction() == REJECT_EVENT && event.getState() == PUBLISHED) {
            log.warn("Попытка отклонить опубликованное событие id={}", eventId);
            throw new ConflictException("Нельзя отклонять опубликованные события");
        }

        updateFields(event, request);
        if (request.getStateAction() != null && request.getStateAction() == PUBLISH_EVENT) {
            event.setPublishedOn(LocalDateTime.now());
        }

        updateEventStatistics(event);
        log.info("Обновлено событие администратором - {}", event);
        return mapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        User user = findUser(userId);
        Event event = findEvent(eventId);

        if (!event.getInitiator().equals(user)) {
            log.warn("Попытка изменить не инициатором событие id={} пользователем id={}", eventId, userId);
            throw new AccessDeniedException("Только инициатор события может вносить в него изменения");
        }
        if (event.getState() == PUBLISHED) {
            log.warn("Попытка изменить уже опубликованное событие id={}", eventId);
            throw new ConflictException("Редактировать можно только неопубликованные события");
        }

        updateFields(event, request);
        updateEventStatistics(event);
        log.info("Обновлено событие пользователем - {}", event);
        return mapper.toFullDto(event);
    }

    @Override
    public List<EventFullDto> getAllByAdmin(SearchParamsAdmin params, int from, int size) {
        PageRequest page = buildPage(from, size);
        List<Event> events = getEventsByParams(params, page);
        updateEventsStatistics(events);
        log.info("Возвращен список событий, запрашиваемых администратором, в размере - {}", events.size());
        return mapper.toFullDto(events);
    }

    @Override
    public List<EventShortDto> getAllByUser(Long userId, int from, int size) {
        PageRequest page = buildPage(from, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, page).getContent();
        updateEventsStatistics(events);
        log.info("Возвращен список событий, запрашиваемых пользователем id={}, в размере - {}", userId, events.size());
        return mapper.toShortDto(events);
    }

    @Override
    public List<EventShortDto> getAll(SearchParamsPublic params, EventSort sort, int from, int size) {
        PageRequest page = buildPage(from, size);

        List<Event> events = getEventsByParams(params, page);

        if (Boolean.TRUE.equals(params.getAvailable())) {
            events = events.stream()
                    .filter(event -> event.getParticipantLimit() == 0
                            || event.getParticipantLimit() > requestRepository.countConfirmedByEventId(event.getId()))
                    .toList();
        }

        updateEventsStatistics(events);
        events = sortBy(events, sort);
        log.info("Возвращен список событий в размере - {}", events.size());
        return mapper.toShortDto(events);
    }

    @Override
    public EventFullDto getByUser(Long userId, Long eventId) {
        User user = findUser(userId);
        Event event = findEvent(eventId);

        if (!event.getInitiator().equals(user)) {
            log.warn("Попытка получить не инициатором событие id={} пользователем id={}", eventId, userId);
            throw new AccessDeniedException("Только инициатор события может просматривать событие");
        }

        updateEventStatistics(event);
        log.info("Возвращено пользователю id={} событие - {}", userId, event);
        return mapper.toFullDto(event);
    }

    @Override
    public EventFullDto getById(Long eventId) {
        Event event = eventRepository.findPublished(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        updateEventStatistics(event);
        log.info("Возвращено событие - {}", event);
        return mapper.toFullDto(event);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + categoryId + " не найдена"));
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    private <T extends BaseUpdateEventRequest> void updateFields(Event entity, T request) {
        Optional.ofNullable(request.getTitle()).ifPresent(entity::setTitle);
        Optional.ofNullable(request.getAnnotation()).ifPresent(entity::setAnnotation);
        Optional.ofNullable(request.getCategory()).ifPresent(id -> entity.setCategory(findCategory(id)));
        Optional.ofNullable(request.getPaid()).ifPresent(entity::setPaid);
        Optional.ofNullable(request.getEventDate()).ifPresent(entity::setEventDate);
        Optional.ofNullable(request.getDescription()).ifPresent(entity::setDescription);
        Optional.ofNullable(request.getParticipantLimit()).ifPresent(entity::setParticipantLimit);
        Optional.ofNullable(request.getStateAction()).ifPresent(state -> entity.setState( switch (state) {
            case CANCEL_REVIEW, REJECT_EVENT -> CANCELED;
            case PUBLISH_EVENT -> PUBLISHED;
            case SEND_TO_REVIEW -> PENDING;
        }));
        Optional.ofNullable(request.getLocation()).ifPresent(location -> {
            Optional.ofNullable(location.getLat()).ifPresent(entity::setLocationLat);
            Optional.ofNullable(location.getLon()).ifPresent(entity::setLocationLon);
        });
        Optional.ofNullable(request.getRequestModeration()).ifPresent(entity::setRequestModeration);
    }

    private PageRequest buildPage(int from, int size) {
        return PageRequest.of(from > 0 ? from / size : 0, size);
    }

    private <T extends SearchParamsBase> List<Event> getEventsByParams(T params, PageRequest page) {
        if (params.hasFilters()) {
            BooleanExpression expression = buildExpression(params);
            return expression != null
                    ? eventRepository.findAll(expression, page).getContent()
                    : Collections.emptyList();
        } else {
            return eventRepository.findAll(page).getContent();
        }
    }

    private List<Event> sortBy(List<Event> events, EventSort sort) {
        return switch (sort) {
            case EVENT_DATE -> events.stream()
                    .sorted(Comparator.comparing(Event::getEventDate))
                    .toList();
            case VIEWS -> events.stream()
                    .sorted(Comparator.comparing(Event::getViews).reversed())
                    .toList();
        };
    }

    private <T extends SearchParamsBase> BooleanExpression buildExpression(T params) {
        List<BooleanExpression> expressions = new ArrayList<>();

        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            expressions.add(QEvent.event.category.id.in(params.getCategories()));
        }
        if (params.getRangeStart() != null) {
            expressions.add(QEvent.event.eventDate.goe(params.getRangeStart()));
        }
        if (params.getRangeEnd() != null) {
            expressions.add(QEvent.event.eventDate.loe(params.getRangeEnd()));
        }

        if (params instanceof SearchParamsAdmin paramsAdmin) {
            buildAdminExpression(expressions, paramsAdmin);
        } else if (params instanceof SearchParamsPublic paramsPublic) {
            buildPublicExpression(expressions, paramsPublic);
        }

        return expressions.stream()
                .reduce(BooleanExpression::and)
                .orElse(null);
    }

    private void buildAdminExpression(List<BooleanExpression> expressions, SearchParamsAdmin params) {
        if (params.getUsers() != null && !params.getUsers().isEmpty()) {
            expressions.add(QEvent.event.initiator.id.in(params.getUsers()));
        }
        if (params.getStates() != null && !params.getStates().isEmpty()) {
            expressions.add(QEvent.event.state.in(params.getStates()));
        }
    }

    private void buildPublicExpression(List<BooleanExpression> expressions, SearchParamsPublic params) {
        expressions.add(QEvent.event.state.eq(PUBLISHED));
        if (params.getText() != null && !params.getText().isBlank()) {
            expressions.add(QEvent.event.annotation.containsIgnoreCase(params.getText())
                    .or(QEvent.event.description.containsIgnoreCase(params.getText())));
        }
        if (params.getPaid() != null) {
            expressions.add(QEvent.event.paid.eq(params.getPaid()));
        }
        if (params.getRangeStart() == null && params.getRangeEnd() == null) {
            expressions.add(QEvent.event.eventDate.goe(LocalDateTime.now()));
        }
    }

    private void updateEventStatistics(Event event) {
        event.setConfirmedRequests(requestRepository.countConfirmedByEventId(event.getId()));
        event.setViews(getEventViews(event.getId()));
    }

    private void updateEventsStatistics(List<Event> events) {
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> countsRequests = requestRepository.getCountConfirmedMap(eventIds);
        events.forEach(event -> event.setConfirmedRequests(countsRequests.getOrDefault(event.getId(), 0L)));

        Map<Long, Long> views = getEventsViews(eventIds);
        events.forEach(event -> event.setViews(views.getOrDefault(event.getId(), 0L)));
    }

    private Map<Long, Long> getEventsViews(List<Long> eventIds) {
        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        return client.get(
                        LocalDateTime.now().withDayOfYear(1),
                        LocalDateTime.now().withDayOfYear(365),
                        uris,
                        true
                )
                .stream()
                .collect(Collectors.toMap(
                        viewStat -> {
                            String uri = viewStat.getUri();
                            return Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1));
                        },
                        ViewStats::getHits
                ));
    }

    private Long getEventViews(Long eventId) {
        return getEventsViews(List.of(eventId)).getOrDefault(eventId, 0L);
    }
}
