package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.entity.Hit;
import ru.practicum.ewm.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {
    @Query("""
            SELECT NEW ru.practicum.ewm.ViewStats(
                h.app,
                h.uri,
                CASE WHEN :unique = true THEN COUNT(DISTINCT h.ip) ELSE COUNT(h.ip) END
            )
            FROM Hit h
            WHERE h.timestamp BETWEEN :start AND :end
                AND (:uris IS NULL OR h.uri IN :uris)
            GROUP BY h.app, h.uri
            ORDER BY CASE WHEN :unique = true THEN COUNT(DISTINCT h.ip) ELSE COUNT(h.ip) END DESC
            """)
    List<ViewStats> find(LocalDateTime start,
                         LocalDateTime end,
                         @Param("uris") List<String> uris,
                         @Param("unique") boolean unique);
}
