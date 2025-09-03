package ru.practicum.ewm.repository.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.request.RequestStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface RequestRepository extends JpaRepository<Request, Long> {
    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    boolean existsByRequesterIdAndEventIdAndStatus(Long userId, Long eventId, RequestStatus status);

    List<Request> findByRequesterId(Long userId);

    List<Request> findAllByIdIn(List<Long> ids);

    List<Request> findByEventId(Long eventId);

    @Query("SELECT COUNT(r) FROM Request r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    Long countConfirmedByEventId(@Param("eventId") Long eventId);

    @Query("""
            SELECT r.event.id, COUNT(r)
            FROM Request r
            WHERE r.event.id IN :eventIds
            AND r.status = 'CONFIRMED'
            GROUP BY r.event.id
            """)
    List<Object[]> countConfirmedByEventIds(@Param("eventIds") List<Long> eventIds);

    default Map<Long, Long> getCountConfirmedMap(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return countConfirmedByEventIds(eventIds).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));
    }
}
