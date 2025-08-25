package ru.practicum.ewm.dto.event;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SearchParamsPublic extends SearchParamsBase {
    private final String text;
    private final Boolean paid;
    private final Boolean available;

    @Override
    protected boolean hasSpecFilters() {
        return text != null || paid != null || !available;
    }
}
