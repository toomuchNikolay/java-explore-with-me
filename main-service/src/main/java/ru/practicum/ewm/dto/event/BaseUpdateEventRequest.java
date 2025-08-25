package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.model.event.StateAction;

import java.time.LocalDateTime;

import static ru.practicum.ewm.util.DateTimeFormats.STANDARD;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class BaseUpdateEventRequest {
    @Size(min = 3, max = 120)
    private String title;

    @Size(min = 20, max = 2000)
    private String annotation;

    @Positive
    private Long category;

    private Boolean paid;

    @JsonFormat(pattern = STANDARD)
    private LocalDateTime eventDate;

    @Size(min = 20, max = 7000)
    private String description;

    @PositiveOrZero
    private Integer participantLimit;

    @Valid
    private Location location;

    private Boolean requestModeration;

    private StateAction stateAction;
}
