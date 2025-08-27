package ru.practicum.ewm.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.exception.custom.AccessDeniedException;
import ru.practicum.ewm.exception.custom.ConflictException;
import ru.practicum.ewm.exception.custom.InvalidParameterException;
import ru.practicum.ewm.exception.custom.NotFoundException;
import ru.practicum.ewm.mapper.request.RequestMapper;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.request.RequestStatus;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.event.EventRepository;
import ru.practicum.ewm.repository.request.RequestRepository;
import ru.practicum.ewm.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.ewm.model.event.EventState.*;
import static ru.practicum.ewm.model.request.RequestStatus.*;
import static ru.practicum.ewm.model.request.RequestStatus.CANCELED;
import static ru.practicum.ewm.model.request.RequestStatus.PENDING;
import static ru.practicum.ewm.util.ParserUtils.parseEnum;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper mapper;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User user = findUser(userId);
        Event event = findEvent(eventId);

        validateEventStatusIsPublished(event);
        if (isUserInitiatorOfEvent(event, user)) {
            log.warn("Попытка отправить запрос на участие в своем же событии");
            throw new ConflictException("Запрещено отправлять запросы на участие в своих же событиях");
        }
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            log.warn("Попытка отправить более одного запроса на участие в событии");
            throw new ConflictException("Запрещено отправлять более одного запроса на участие в событии");
        }
        if (availableParticipantLimit(event) == 0) {
            log.warn("Попытка отправить запрос на событие без свободных мест");
            throw new ConflictException("Достигнут лимит по количеству участников в событии");
        }

        Request request = requestRepository.save(Request.builder()
                .event(event)
                .requester(user)
                .status((!event.getRequestModeration() || event.getParticipantLimit() == 0) ? CONFIRMED : PENDING)
                .created(LocalDateTime.now())
                .build());
        log.info("Добавлен запрос на участие в событии - {}", request);
        return mapper.toDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        User user = findUser(userId);
        Request request = findRequest(requestId);

        if (!request.getRequester().equals(user)) {
            log.warn("Попытка отменить событие пользователем id={} вместо инициатора", userId);
            throw new AccessDeniedException("Отменить запрос на событие может только инициатор");
        }

        request.setStatus(CANCELED);
        return mapper.toDto(request);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        User user = findUser(userId);
        Event event = findEvent(eventId);

        validateEventStatusIsPublished(event);
        if (!isUserInitiatorOfEvent(event, user)) {
            log.warn("Попытка изменить заявки не инициатором события");
            throw new AccessDeniedException("Изменять статусы заявок может только инициатор события");
        }

        List<Request> requests = requestRepository.findAllByIdIn(request.getRequestIds());
        RequestStatus status = parseEnum(request.getStatus(), RequestStatus.class);

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();

        switch (status) {
            case CONFIRMED -> {
                long availableParticipants = availableParticipantLimit(event);
                for (Request req : requests) {
                    validateRequestStatusIsPending(req);
                    if (availableParticipants > 0) {
                        req.setStatus(CONFIRMED);
                        confirmedRequests.add(req);
                        availableParticipants--;
                    } else {
                        throw new ConflictException("Исчерпан лимит свободных мест на участие в событии");
                    }
                }
            }
            case REJECTED -> {
                for (Request req : requests) {
                    validateRequestStatusIsPending(req);
                    req.setStatus(REJECTED);
                    rejectedRequests.add(req);
                }
            }
            default -> throw new InvalidParameterException("Передан неподдерживаемый параметр: " + request.getStatus());
        }

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(mapper.toDto(confirmedRequests))
                .rejectedRequests(mapper.toDto(rejectedRequests))
                .build();
    }

    @Override
    public List<ParticipationRequestDto> getAllRequestByUser(Long userId) {
        User user = findUser(userId);
        List<Request> requests = requestRepository.findByRequesterId(user.getId());
        log.info("Возвращен список заявок пользователя в размере - {}", requests.size());
        return mapper.toDto(requests);
    }

    @Override
    public List<ParticipationRequestDto> getALlRequestByEvent(Long userId, Long eventId) {
        User user = findUser(userId);
        Event event = findEvent(eventId);

        if (!event.getInitiator().equals(user)) {
            log.warn("Попытка получить список участников не инициатором события");
            throw new AccessDeniedException("Просматривать заявки на участие в событии может только его инициатор");
        }

        List<Request> requests = requestRepository.findByEventId(eventId);
        log.info("Возвращен список участников в событии в размере - {}", requests.size());
        return mapper.toDto(requests);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    private Request findRequest(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос на участие в событии с id=" + requestId + " не найден"));
    }

    private void validateEventStatusIsPublished(Event event) {
        if (event.getState() != PUBLISHED) {
            throw new ConflictException("Запрещено оставлять запросы на участие в неопубликованных событиях");
        }
    }

    private boolean isUserInitiatorOfEvent(Event event, User user) {
        return event.getInitiator().equals(user);
    }

    private long availableParticipantLimit(Event event) {
        return event.getParticipantLimit() != 0
                ? event.getParticipantLimit() - requestRepository.countConfirmedByEventId(event.getId())
                : -1;
    }

    private void validateRequestStatusIsPending(Request request) {
        if (request.getStatus() != PENDING) {
            throw new ConflictException("Заявка на участие должна быть в статусе ожидания");
        }
    }
}
