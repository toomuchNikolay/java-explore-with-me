package ru.practicum.ewm.service.reaction;

import ru.practicum.ewm.dto.reaction.RatingEventDto;
import ru.practicum.ewm.dto.reaction.RatingUserDto;
import ru.practicum.ewm.dto.reaction.ReactionDto;
import ru.practicum.ewm.model.reaction.RatingPeriod;

import java.time.LocalDateTime;
import java.util.List;

public interface ReactionService {
    ReactionDto create(Long userId, Long eventId, Boolean isLike, LocalDateTime time);

    List<ReactionDto> getReactionsByAdmin(Long userId, Long eventId, Boolean isLike, int from, int size);

    List<ReactionDto> getReactionsByUser(Long userId);

    List<ReactionDto> getReactionsByEvent(Long userId, Long eventId);

    List<RatingEventDto> getPopularEvents(int limit, RatingPeriod period);

    List<RatingUserDto> getPopularUsers(int limit, RatingPeriod period);

    RatingEventDto getEventRating(Long eventId);

    RatingUserDto getUserRating(Long userId);

    void deleteByAdmin(Long reactionId);

    void deleteByUser(Long userId, Long eventId);
}
