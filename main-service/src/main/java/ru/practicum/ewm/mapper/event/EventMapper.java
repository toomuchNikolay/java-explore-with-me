package ru.practicum.ewm.mapper.event;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class, EventState.class})
public interface EventMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "locationLat", source = "dto.location.lat")
    @Mapping(target = "locationLon", source = "dto.location.lon")
    @Mapping(target = "paid", source = "dto.paid", defaultValue = "false")
    @Mapping(target = "participantLimit", source = "dto.participantLimit", defaultValue = "0")
    @Mapping(target = "requestModeration", source = "dto.requestModeration", defaultValue = "true")
    @Mapping(target = "state", expression = "java(EventState.PENDING)")
    @Mapping(target = "createdOn", expression = "java(LocalDateTime.now())")
    Event toEntity(NewEventDto dto, Category category, User initiator);

    @Named("toShortDto")
    EventShortDto toShortDto(Event event);

    @IterableMapping(qualifiedByName = "toShortDto")
    List<EventShortDto> toShortDto(List<Event> events);

    @Mapping(target = "location", expression = "java(new Location(event.getLocationLat(), event.getLocationLon()))")
    @Named("toFullDto")
    EventFullDto toFullDto(Event event);

    @IterableMapping(qualifiedByName = "toFullDto")
    List<EventFullDto> toFullDto(List<Event> events);
}
