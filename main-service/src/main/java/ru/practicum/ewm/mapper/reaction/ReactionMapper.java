package ru.practicum.ewm.mapper.reaction;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.reaction.ReactionDto;
import ru.practicum.ewm.model.reaction.Reaction;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReactionMapper {
    @Mapping(source = "user.id", target = "user")
    @Mapping(source = "event.id", target = "event")
    ReactionDto toDto(Reaction reaction);

    List<ReactionDto> toDto(List<Reaction> reactions);
}
