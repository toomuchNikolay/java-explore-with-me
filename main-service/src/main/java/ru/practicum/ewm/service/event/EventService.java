package ru.practicum.ewm.service.event;

import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.model.event.EventSort;

import java.util.List;

public interface EventService {
    EventFullDto create(Long userId, NewEventDto dto);

    EventFullDto updateByAdmin(Long eventId, UpdateEventAdminRequest request);

    EventFullDto updateByUser(Long userId, Long eventId, UpdateEventUserRequest request);

    List<EventFullDto> getAllByAdmin(SearchParamsAdmin params, int from, int size);

    List<EventShortDto> getAllByUser(Long userId, int from, int size);

    List<EventShortDto> getAll(SearchParamsPublic params, EventSort sort, int from, int size);

    EventFullDto getByUser(Long userId, Long eventId);

    EventFullDto getById(Long eventId);
}
