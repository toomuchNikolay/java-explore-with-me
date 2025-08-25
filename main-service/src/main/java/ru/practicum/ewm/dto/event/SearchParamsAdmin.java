package ru.practicum.ewm.dto.event;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.model.event.EventState;

import java.util.List;

@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SearchParamsAdmin extends SearchParamsBase {
    private final List<Long> users;
    private final List<EventState> states;

    @Override
    protected boolean hasSpecFilters() {
        return users != null || states != null;
    }
}
