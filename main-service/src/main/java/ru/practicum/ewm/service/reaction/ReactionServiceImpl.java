package ru.practicum.ewm.service.reaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.reaction.RatingEventDto;
import ru.practicum.ewm.dto.reaction.RatingUserDto;
import ru.practicum.ewm.dto.reaction.ReactionDto;
import ru.practicum.ewm.exception.custom.AccessDeniedException;
import ru.practicum.ewm.exception.custom.ConflictException;
import ru.practicum.ewm.exception.custom.NotFoundException;
import ru.practicum.ewm.mapper.event.EventMapper;
import ru.practicum.ewm.mapper.reaction.ReactionMapper;
import ru.practicum.ewm.mapper.user.UserMapper;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.reaction.RatingPeriod;
import ru.practicum.ewm.model.reaction.Reaction;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.event.EventRepository;
import ru.practicum.ewm.repository.reaction.ReactionRepository;
import ru.practicum.ewm.repository.request.RequestRepository;
import ru.practicum.ewm.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static ru.practicum.ewm.model.request.RequestStatus.CONFIRMED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReactionServiceImpl implements ReactionService {
    private final ReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final ReactionMapper reactionMapper;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ReactionDto create(Long userId, Long eventId, Boolean isLike, LocalDateTime time) {
        User user = findUser(userId);
        Event event = findEvent(eventId);
        if (event.getInitiator().equals(user)) {
            log.info("Попытка оценить свое событие");
            throw new ConflictException("Оценивать свое событие запрещено");
        }
        if (event.getEventDate().isAfter(time)) {
            log.info("Попытка оценить событие, которое еще не началось");
            throw new ConflictException("Оценивать событие нельзя до его начала");
        }
        if (!requestRepository.existsByRequesterIdAndEventIdAndStatus(userId, eventId, CONFIRMED)) {
            log.info("Попытка оценить событие пользователем, который не является его участником");
            throw new AccessDeniedException("Оценивать событие могут только его участники");
        }

        Reaction reaction = reactionRepository.save(Reaction.builder()
                .user(user)
                .event(event)
                .isLike(isLike)
                .created(LocalDateTime.now())
                .build());
        log.info("Добавлена реакция на событие - {}", reaction);
        return reactionMapper.toDto(reaction);
    }

    @Override
    public List<ReactionDto> getReactionsByAdmin(Long userId, Long eventId, Boolean isLike, int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Reaction> reactions;
        if (userId != null || eventId != null || isLike != null) {
            reactions = reactionRepository.findByAdminParams(userId, eventId, isLike, page).getContent();
        } else {
            reactions = reactionRepository.findAll(page).getContent();
        }
        log.info("Возвращен список оценок администратору в размере - {}", reactions.size());
        return reactionMapper.toDto(reactions);
    }

    @Override
    public List<ReactionDto> getReactionsByUser(Long userId) {
        List<Reaction> reactions = reactionRepository.findAllByUserId(userId);
        log.info("Возвращен список оценок пользователя id={}", userId);
        return reactionMapper.toDto(reactions);
    }

    @Override
    public List<ReactionDto> getReactionsByEvent(Long userId, Long eventId) {
        User user = findUser(userId);
        Event event = findEvent(eventId);
        if (!event.getInitiator().equals(user)) {
            throw new AccessDeniedException("Просматривать оценки можно только на свои события");
        }

        List<Reaction> reactions = reactionRepository.findAllByEventId(eventId);
        log.info("Возвращен список оценок события id={}", eventId);
        return reactionMapper.toDto(reactions);
    }

    @Override
    public List<RatingEventDto> getPopularEvents(int limit, RatingPeriod period) {
        LocalDateTime time = findStartPeriodDateTime(period);
        List<RatingEventDto> result = reactionRepository.getPopularEvents(limit, time).entrySet()
                .stream()
                .map(this::buildRatingEventDto)
                .toList();
        log.info("Возвращен список из {} популярных событий за период {}", limit, period);
        return result;
    }

    @Override
    public List<RatingUserDto> getPopularUsers(int limit, RatingPeriod period) {
        LocalDateTime time = findStartPeriodDateTime(period);
        List<RatingUserDto> result = reactionRepository.getPopularUsers(limit, time).entrySet()
                .stream()
                .map(this::buildRatingUserDto)
                .toList();
        log.info("Возвращен список из {} популярных пользователей за период {}", limit, period);
        return result;
    }

    @Override
    public RatingEventDto getEventRating(Long eventId) {
        Event event = findEvent(eventId);
        Long rating = reactionRepository.countEventRating(eventId);
        RatingEventDto result = RatingEventDto.builder()
                .event(eventMapper.toShortDto(event))
                .rating(rating)
                .build();
        log.info("Возвращен рейтинг события - {}", result);
        return result;
    }

    @Override
    public RatingUserDto getUserRating(Long userId) {
        User user = findUser(userId);
        Long rating = reactionRepository.countUserRating(userId);
        RatingUserDto result = RatingUserDto.builder()
                .user(userMapper.toShortDto(user))
                .rating(rating)
                .build();
        log.info("Возвращен рейтинг пользователя - {}", result);
        return result;
    }

    @Override
    @Transactional
    public void deleteByAdmin(Long reactionId) {
        Reaction reaction = reactionRepository.findById(reactionId)
                .orElseThrow(() -> new NotFoundException("Оценка с id=" + reactionId + " не найдена"));
        reactionRepository.delete(reaction);
        log.info("Удалена администратором оценка - {}", reaction);
    }

    @Override
    @Transactional
    public void deleteByUser(Long userId, Long eventId) {
        Reaction reaction = reactionRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new NotFoundException("Оценка пользователем id=" + userId + " событию id=" + eventId
                        + " не найдена"));
        reactionRepository.delete(reaction);
        log.info("Удалена пользователем оценка - {}", reaction);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    private LocalDateTime findStartPeriodDateTime(RatingPeriod period) {
        return switch (period) {
            case WEEK -> LocalDateTime.now().minusWeeks(1);
            case MONTH -> LocalDateTime.now().minusMonths(1);
            case YEAR -> LocalDateTime.now().minusYears(1);
        };
    }

    private RatingEventDto buildRatingEventDto(Map.Entry<Long, Long> entry) {
        return RatingEventDto.builder()
                .event(eventMapper.toShortDto(findEvent(entry.getKey())))
                .rating(entry.getValue())
                .build();
    }

    private RatingUserDto buildRatingUserDto(Map.Entry<Long, Long> entry) {
        return RatingUserDto.builder()
                .user(userMapper.toDto(findUser(entry.getKey())))
                .rating(entry.getValue())
                .build();
    }
}
