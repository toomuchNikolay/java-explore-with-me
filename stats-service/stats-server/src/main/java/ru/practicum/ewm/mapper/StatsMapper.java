package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.EndpointHit;
import ru.practicum.ewm.entity.Hit;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    @Mapping(target = "id", ignore = true)
    Hit toEntity(EndpointHit endpointHit);
}
