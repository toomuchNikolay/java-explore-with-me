package ru.practicum.ewm.repository.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.event.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    boolean existsByCategoryId(Long id);

    Page<Event> findAllByInitiatorId(Long id, Pageable page);

    @Query("SELECT e FROM Event e WHERE e.state = 'PUBLISHED' AND e.id = :id")
    Optional<Event> findPublished(@Param("id") Long id);

    List<Event> findAllByIdIn(List<Long> ids);
}
