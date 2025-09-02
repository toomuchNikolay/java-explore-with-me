package ru.practicum.ewm.repository.reaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.reaction.Reaction;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    List<Reaction> findAllByUserId(Long userId);

    List<Reaction> findAllByEventId(Long eventId);

    Optional<Reaction> findByUserIdAndEventId(Long userId, Long eventId);

    @Query("""
            SELECT r
            FROM Reaction r
            WHERE (:userId IS NULL OR r.user.id = :userId)
                AND (:eventId IS NULL OR r.event.id = :eventId)
                AND (:isLike IS NULL OR r.isLike = :isLike)
            """)
    Page<Reaction> findByAdminParams(@Param("userId") Long userId,
                                     @Param("eventId") Long eventId,
                                     @Param("isLike") Boolean isLike,
                                     Pageable page);

    @Query("""
            SELECT SUM(CASE WHEN r.isLike = true THEN 1 ELSE -1 END)
            FROM Reaction r
            WHERE r.event.id = :eventId
            """)
    Long countEventRating(@Param("eventId") Long eventId);

    @Query("""
            SELECT SUM(CASE WHEN r.isLike = true THEN 1 ELSE -1 END)
            FROM Reaction r
            WHERE r.event.initiator.id = :userId
            """)
    Long countUserRating(@Param("userId") Long userId);

    @Query("""
            SELECT r.event.id, SUM(CASE WHEN r.isLike = true THEN 1 ELSE -1 END) AS rating
            FROM Reaction r
            WHERE r.event.eventDate >= :time
            GROUP BY r.event.id
            ORDER BY rating DESC
            LIMIT :limit
            """)
    List<Object[]> findPopularEvents(@Param("limit") int limit, @Param("time") LocalDateTime time);

    default Map<Long, Long> getPopularEvents(int limit, LocalDateTime time) {
        return convertResultToMap(findPopularEvents(limit, time));
    }

    @Query("""
            SELECT r.event.initiator.id,
                SUM(CASE WHEN r.isLike = true THEN 1 ELSE -1 END) AS rating
            FROM Reaction r
            WHERE r.event.eventDate >= :time
            GROUP BY r.event.initiator.id
            ORDER BY rating DESC
            LIMIT :limit
            """)
    List<Object[]> findPopularUsers(@Param("limit") int limit, @Param("time") LocalDateTime time);

    default Map<Long, Long> getPopularUsers(int limit, LocalDateTime time) {
        return convertResultToMap(findPopularUsers(limit, time));
    }

    private Map<Long, Long> convertResultToMap(List<Object[]> result) {
        Map<Long, Long> map = new LinkedHashMap<>();
        for (Object[] arr : result) {
            map.put((Long) arr[0], (Long) arr[1]);
        }
        return map;
    }
}
