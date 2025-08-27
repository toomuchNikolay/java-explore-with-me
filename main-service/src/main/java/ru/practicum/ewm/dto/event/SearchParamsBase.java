package ru.practicum.ewm.dto.event;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@SuperBuilder(toBuilder = true)
public abstract class SearchParamsBase {
    private final List<Long> categories;
    private final LocalDateTime rangeStart;
    private final LocalDateTime rangeEnd;

    public final boolean hasFilters() {
        return getCategories() != null || getRangeStart() != null || getRangeEnd() != null || hasSpecFilters();
    }

    protected abstract boolean hasSpecFilters();
}
